package eu.strator.samlsample;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

public final class EncryptionUtilsSample {

    public static PrivateKey getPrivateKeyFromResource(Resource pKResource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        KeySpec ks = new PKCS8EncodedKeySpec(Base64Sample.decode(removePrivateKeyTags(pKResource)));

        return keyFactory.generatePrivate(ks);
    }

    private static byte[] removePrivateKeyTags(Resource pKResource) throws IOException {
        Scanner scanner = new Scanner(pKResource.getInputStream());
        StringBuilder fileContentBuilder = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains("PRIVATE")) {
                fileContentBuilder.append(line);
            }
        }
        scanner.close();

        return fileContentBuilder.toString().getBytes();
    }

    public static X509Certificate getX509Certificate(Resource certResource) throws IOException, CertificateException {
        InputStream crtStream = certResource.getInputStream();
        CertificateFactory certFact = CertificateFactory.getInstance("X.509");
        try {
            return (X509Certificate) certFact.generateCertificate(crtStream);
        } finally {
            if (crtStream != null) {
                crtStream.close();
            }
        }
    }
}
