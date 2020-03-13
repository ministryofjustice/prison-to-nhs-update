package uk.gov.justice.digital.hmpps.prisontonhs.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class WebClientConfiguration() {

  @Bean
  open fun oauth2WebClient(authorizedClientManager: OAuth2AuthorizedClientManager?): WebClient? {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs{configurer  ->
                      configurer.defaultCodecs()
                              .maxInMemorySize(-1)}
                    .build())
            .build()
  }

  @Bean
  open fun webClient(): WebClient? {
    return WebClient.builder().build()
  }

  @Bean
  open fun authorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository?,
                                   oAuth2AuthorizedClientService: OAuth2AuthorizedClientService?): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
