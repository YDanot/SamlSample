package eu.strator.samlsample;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-saml-context.xml"})
public class SAMLResponseBuilderTest {

    @Autowired
    private SAMLResponseSignerSample samlResponseSigner;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SAMLResponseBuilderSample samlResponseBuilder;

    @Test
    public void testBuildAssertion() throws Exception {
        Response response = samlResponseBuilder.buildResponse();
        samlResponseSigner.sign(response);
        validateSignature(response);
    }

    public void validateSignature(Response response) throws IOException, CertificateException {
        Resource rightCertResource = applicationContext.getResource("classpath:/idp.tpos.logista.com.crt");
        Resource wrongCertResource = applicationContext.getResource("classpath:/wrongCertificate.crt");

        Assertions.assertThat(validateResponseWithCertificate(response, rightCertResource)).as("validation de la signature avec la cle publique utilisée lors de la création de la SAMLResponse").isTrue();
        Assertions.assertThat(validateResponseWithCertificate(response, wrongCertResource)).as("validation de la signature avec une autre cle publique").isFalse();
    }

    private Boolean validateResponseWithCertificate(Response response, Resource certResource) throws IOException, CertificateException {
        try {
            BasicX509Credential publicCredential = new BasicX509Credential();
            PublicKey publicKey = EncryptionUtilsSample.getX509Certificate(certResource).getPublicKey();
            publicCredential.setPublicKey(publicKey);

            response.getDOM().setIdAttribute("ID", true);
            for (Assertion assertion : response.getAssertions()) {
                assertion.getDOM().setIdAttribute("ID", true);
            }

            SignatureValidator signatureValidator = new SignatureValidator(publicCredential);
            signatureValidator.validate(response.getSignature());
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }
    
}