package com.twilio.conversations.app.common

import android.app.Application
import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import com.twilio.conversations.app.data.ConversationsClientWrapper
import com.twilio.conversations.app.data.CredentialStorage
import com.twilio.conversations.app.manager.ConversationListManagerImpl
import com.twilio.conversations.app.manager.FCMManager
import com.twilio.conversations.app.manager.FCMManagerImpl
import com.twilio.conversations.app.manager.LoginManager
import com.twilio.conversations.app.manager.LoginManagerImpl
import com.twilio.conversations.app.manager.MessageListManagerImpl
import com.twilio.conversations.app.manager.ParticipantListManagerImpl
import com.twilio.conversations.app.manager.UserManagerImpl
import com.twilio.conversations.app.repository.ConversationsRepositoryImpl
import com.twilio.conversations.app.viewModel.ConversationDetailsViewModel
import com.twilio.conversations.app.viewModel.ConversationListViewModel
import com.twilio.conversations.app.viewModel.LoginViewModel
import com.twilio.conversations.app.viewModel.MessageListViewModel
import com.twilio.conversations.app.viewModel.ParticipantListViewModel
import com.twilio.conversations.app.viewModel.SplashViewModel

var injector = Injector()
    private set

@RestrictTo(Scope.TESTS)
fun setupTestInjector(testInjector: Injector) {
    injector = testInjector
}

open class Injector {

    private var fcmManagerImpl: FCMManagerImpl? = null

    open fun createCredentialStorage(applicationContext: Context) = CredentialStorage(applicationContext)

    open fun createLoginManager(applicationContext: Context): LoginManager = LoginManagerImpl(
        ConversationsClientWrapper.INSTANCE,
        ConversationsRepositoryImpl.INSTANCE,
        CredentialStorage(applicationContext),
        FirebaseTokenManager(),
    )

    open fun createLoginViewModel(application: Application): LoginViewModel {
        val loginManager = createLoginManager(application)

        return LoginViewModel(loginManager)
    }

    open fun createSplashViewModel(application: Application): SplashViewModel {
        val loginManager = createLoginManager(application)

        val viewModel = SplashViewModel(loginManager, application)
        viewModel.initialize()

        return viewModel
    }

    open fun createConversationListViewModel(application: Application): ConversationListViewModel {
        val conversationListManager = ConversationListManagerImpl(ConversationsClientWrapper.INSTANCE)
        val userManager = UserManagerImpl(ConversationsClientWrapper.INSTANCE)
        val loginManager = createLoginManager(application)

        return ConversationListViewModel(ConversationsRepositoryImpl.INSTANCE, conversationListManager, userManager, loginManager)
    }

    open fun createMessageListViewModel(appContext: Context, conversationSid: String): MessageListViewModel {
        val messageListManager = MessageListManagerImpl(conversationSid, ConversationsClientWrapper.INSTANCE, ConversationsRepositoryImpl.INSTANCE)
        return MessageListViewModel(appContext, conversationSid, ConversationsRepositoryImpl.INSTANCE, messageListManager)
    }

    open fun createConversationDetailsViewModel(conversationSid: String): ConversationDetailsViewModel {
        val conversationListManager = ConversationListManagerImpl(ConversationsClientWrapper.INSTANCE)
        val participantListManager = ParticipantListManagerImpl(conversationSid, ConversationsClientWrapper.INSTANCE)
        return ConversationDetailsViewModel(conversationSid, ConversationsRepositoryImpl.INSTANCE, conversationListManager, participantListManager)
    }

    open fun createParticipantListViewModel(conversationSid: String): ParticipantListViewModel {
        val participantListManager = ParticipantListManagerImpl(conversationSid, ConversationsClientWrapper.INSTANCE)
        return ParticipantListViewModel(conversationSid, ConversationsRepositoryImpl.INSTANCE, participantListManager)
    }

    open fun createFCMManager(context: Context): FCMManager {
        val credentialStorage = createCredentialStorage(context.applicationContext)
        if (fcmManagerImpl == null) {
            fcmManagerImpl = FCMManagerImpl(context, ConversationsClientWrapper.INSTANCE, credentialStorage)
        }
        return fcmManagerImpl!!
    }
}
