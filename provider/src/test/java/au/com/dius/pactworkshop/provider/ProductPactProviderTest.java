package au.com.dius.pactworkshop.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import org.apache.hc.core5.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.when;

@Provider("ProductService")
@PactBroker(
        host = "localhost",
        port = "8000",
        authentication = @PactBrokerAuth(username = "pact_workshop", password = "pact_workshop")
)

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductPactProviderTest {
    @au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
      // Select Pacts for consumers deployed or released to production, those on the main branch
      // and those on a named branch step11, for use in our workshop
      return new SelectorBuilder()
        .deployedOrReleased()
        .mainBranch()
        .branch("step11");
    }
    @LocalServerPort
    int port;

    @MockBean
    private ProductRepository productRepository;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context, HttpRequest request) {
        replaceAuthHeader(request);
        context.verifyInteraction();
    }

    private void replaceAuthHeader(HttpRequest request) {
        if (request.containsHeader("Authorization")) {
            String header = "Bearer " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date());
            request.removeHeaders("Authorization");
            request.addHeader("Authorization", header);
        }
    }

    @State("product with ID 10 exists")
    void toProductWithIdTenExistsState() {
        when(productRepository.getById("10")).thenReturn(Optional.of(new Product("10", "CREDIT_CARD", "28 Degrees", "v1")));
    }
}
