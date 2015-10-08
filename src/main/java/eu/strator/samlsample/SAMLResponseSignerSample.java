package eu.strator.samlsample;

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class SAMLResponseSignerSample {

    @Autowired
    private ResourceLoader resourceLoader;

    private String keyPath = "classpath:/idp.tpos.logista.com.key";
    private String certificatePath = "classpath:/idp.tpos.logista.com.crt";

    public Response sign(Response response) throws Exception {
        DefaultBootstrap.bootstrap();

        Signature signature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        BasicCredential credential = computeCredentials();
        signature.setSigningCredential(credential);

        SecurityConfiguration secConfig = Configuration.getGlobalSecurityConfiguration();
        SecurityHelper.prepareSignatureParams(signature, credential, secConfig, null);

        response.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
        Signer.signObject(signature);
        logResponse(response);

        return response;
    }

    private void logResponse(Response response) throws MarshallingException {
        ResponseMarshaller marshaller = new ResponseMarshaller();
        Element plaintextElement = marshaller.marshall(response);
        System.out.println(XMLHelper.nodeToString(plaintextElement));
    }

    private BasicCredential computeCredentials() throws Exception {
        X509Certificate cert = getX509Certificate();
        PrivateKey privKey = getPrivateKey();

        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(cert);
        credential.setPrivateKey(privKey);

        return credential;
    }

    private PrivateKey getPrivateKey() throws Exception {
        Resource pKResource = resourceLoader.getResource(keyPath);
        return EncryptionUtilsSample.getPrivateKeyFromResource(pKResource);
    }

    private X509Certificate getX509Certificate() throws IOException, CertificateException {
        Resource certResource = resourceLoader.getResource(certificatePath);
        return EncryptionUtilsSample.getX509Certificate(certResource);
    }
}
