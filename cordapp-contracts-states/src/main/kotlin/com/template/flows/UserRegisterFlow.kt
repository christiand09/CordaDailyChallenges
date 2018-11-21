package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract.Companion.ID
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RegisterFlow(
                  val ownParty: Party,
                  val name: String,
                  val age: Int,
                  val address: String,
                  val birthDate: String,
                  val status: String,
                  val religion: String,
                  val isVerified: Boolean): FlowLogic<Unit>(){
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(){
        /* Step 1 - Build the transaction */
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val userState =UserState(ownParty,name,age,address,birthDate,status,religion,isVerified)
        val cmd = Command(UserContract.Commands.Register(),ownParty.owningKey)


        val txBuilder = TransactionBuilder(notary)
                .addOutputState(userState,UserContract.ID)
                .addCommand(cmd)
        txBuilder.verify(serviceHub)
        /* Step 2 - Sign the transaction */
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        signedTx.verify(serviceHub)
        /* Step 4 and 5 - Notarize then Record the transaction */
         subFlow(FinalityFlow(signedTx))
    }

}

