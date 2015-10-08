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
public class SAMLResponseSignerTest {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private SAMLResponseSignerSample samlResponseSigner;
    @Autowired private SAMLResponseBuilderSample samlResponseBuilder;

    private Response response;
    private ValidationSignatureState validationSignatureState;

    @Test
    public void signed_reponse_author_should_be_authenticated_with_public_key() throws Exception {
        given_a_response_signed_with_private_key();
        when_we_validate_it_with_public_key();
        then_author_is_confirmed();
    }

    @Test
    public void signed_reponse_author_should_not_be_authenticated_with_wrong_public_key() throws Exception {
        given_a_response_signed_with_private_key();
        when_we_validate_it_with_wrong_public_key();
        then_author_is_not_confirmed();
    }

    private void then_author_is_not_confirmed() {
        Assertions.assertThat(validationSignatureState).as("validation de la signature avec une autre cle publique").isEqualTo(ValidationSignatureState.NOT_VALID);
    }

    private void when_we_validate_it_with_wrong_public_key() throws IOException, CertificateException {
        Resource wrongCertResource = applicationContext.getResource("classpath:/wrongCertificate.crt");
        validationSignatureState = validateResponseWithCertificate(wrongCertResource);
    }

    private void then_author_is_confirmed() {
        Assertions.assertThat(validationSignatureState).as("validation de la signature avec la cle publique utilisée lors de la création de la SAMLResponse").isEqualTo(ValidationSignatureState.VALID);
    }

    private void when_we_validate_it_with_public_key() throws IOException, CertificateException {
        Resource rightCertResource = applicationContext.getResource("classpath:/idp.tpos.logista.com.crt");
        validationSignatureState = validateResponseWithCertificate(rightCertResource);
    }

    private void given_a_response_signed_with_private_key() throws Exception {
        response = samlResponseBuilder.buildResponse();
        samlResponseSigner.sign(response);
    }

    private ValidationSignatureState validateResponseWithCertificate(Resource certResource) throws IOException, CertificateException {
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
            return ValidationSignatureState.NOT_VALID;
        }
        return ValidationSignatureState.VALID;
    }

    private enum ValidationSignatureState {
        VALID,
        NOT_VALID;
    }
}