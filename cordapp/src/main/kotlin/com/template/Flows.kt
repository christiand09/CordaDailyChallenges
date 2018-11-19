package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.KYCContract.Companion.KYC_CONTRACT_ID
import com.template.UserContract.Companion.USER_CONTRACT_ID
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// **************
// * User Flows *
// **************
@InitiatingFlow
@StartableByRPC
class RegisterFlow(val name: String,
                   val age: Int,
                   val address: String,
                   val birthDate: String,
                   val status: String,
                   val religion: String) : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        progressTracker.currentStep = BUILDING_TRANSACTION
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = UserState(
                ourIdentity,
                name,
                age,
                address,
                birthDate,
                status,
                religion)
        val cmd = Command(UserContract.Commands.Register(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, USER_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class UpdateFlow(val name: String,
                 val age: Int,
                 val address: String,
                 val birthDate: String,
                 val status: String,
                 val religion: String) : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        progressTracker.currentStep = BUILDING_TRANSACTION
        val inputCriteria = QueryCriteria.VaultQueryCriteria()
        val inputStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputCriteria).states.first()
//        val input = inputStateAndRef.state.data

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = UserState(
                ourIdentity,
                name,
                age,
                address,
                birthDate,
                status,
                religion)
        val cmd = Command(UserContract.Commands.Update(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputStateAndRef)
                .addOutputState(outputState, USER_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
//        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class VerifyFlow() : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        progressTracker.currentStep = BUILDING_TRANSACTION
        val inputCriteria = QueryCriteria.VaultQueryCriteria()
        val inputStateAndRef = serviceHub.vaultService.queryBy<UserState>(inputCriteria).states.first()
        val input = inputStateAndRef.state.data

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = UserState(
                ourIdentity,
                input.name,
                input.age,
                input.address,
                input.birthDate,
                input.status,
                input.religion,
                true)
        val cmd = Command(UserContract.Commands.Verify(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputStateAndRef)
                .addOutputState(outputState, USER_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
//        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}

// *************
// * KYC Flows *
// *************

@InitiatingFlow
@StartableByRPC
class SendIDFlow() : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        progressTracker.currentStep = BUILDING_TRANSACTION
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = KYCState(
                ourIdentity,
                true)
        val cmd = Command(KYCContract.Commands.Send(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, KYC_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class ValidateFlow() : FlowLogic<Unit>() {

    /* Declare Transaction Steps */
    companion object {
        object BUILDING_TRANSACTION : ProgressTracker.Step("Building Transaction")
        object SIGN_TRANSACTION : ProgressTracker.Step("Signing Transaction")
        object VERIFY_TRANSACTION : ProgressTracker.Step("Verifying Transaction")
        object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
        object RECORD_TRANSACTION : ProgressTracker.Step("Recording Transaction")
    }

    fun tracker() = ProgressTracker(
            BUILDING_TRANSACTION,
            SIGN_TRANSACTION,
            VERIFY_TRANSACTION,
            NOTARIZE_TRANSACTION,
            RECORD_TRANSACTION
    )

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {

        /* Step 1 - Build the transaction */
        progressTracker.currentStep = BUILDING_TRANSACTION
        val inputCriteria = QueryCriteria.VaultQueryCriteria()
        val inputStateAndRef = serviceHub.vaultService.queryBy<KYCState>(inputCriteria).states.first()
        val input = inputStateAndRef.state.data

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val outputState = KYCState(
                ourIdentity,
                input.isSent,
                true)
        val cmd = Command(KYCContract.Commands.Validate(), ourIdentity.owningKey)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, KYC_CONTRACT_ID)
                .addCommand(cmd)

        /* Step 2 - Sign the transaction */
        progressTracker.currentStep = SIGN_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        /* Step 3 - Verify the transaction */
        progressTracker.currentStep = VERIFY_TRANSACTION
//        signedTx.verify(serviceHub)

        /* Step 4 and 5 - Notarize then Record the transaction */
        progressTracker.currentStep = NOTARIZE_TRANSACTION
        progressTracker.currentStep = RECORD_TRANSACTION
        subFlow(FinalityFlow(signedTx))
    }
}