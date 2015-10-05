package eu.strator.samlsample;

import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


@Component
@Scope(value=BeanDefinition.SCOPE_SINGLETON)
public class CommandeLogistadisValidationBeanSample {
    
    private String samlResponseEncoded;
    private String samlRelayStateEncoded;

    @Autowired
    private SAMLResponseSignerSample samlResponseSignerSample;
    @Autowired
    private SAMLResponseBuilderSample samlResponseBuilderSample;

    public String getSamlResponseEncoded() {
        return samlResponseEncoded;
    }

    public String getSamlRelayStateEncoded() {
        return samlRelayStateEncoded;
    }

    private String rpid = "https://testcfsAPPclaims.cloudapp.net";
    private String finalUrl = "https://testcfsappclaims.cloudapp.net/?hello";

    @PostConstruct
    private void initBean() throws Exception {
        initSamlReponseEncoded();
        initRelayState();
    }

    private void initRelayState() throws UnsupportedEncodingException {
        String rpidEncoded = URLEncoder.encode(rpid, "UTF-8");
        String finalUrlEncoded = URLEncoder.encode(finalUrl, "UTF-8");
        samlRelayStateEncoded = "RPID=" + rpidEncoded + "&RelayState=" + finalUrlEncoded;
    }

    private void initSamlReponseEncoded() throws Exception {
        Response response = samlResponseSignerSample.sign(samlResponseBuilderSample.buildResponse());
        byte[] responseEncoded = Base64Sample.encode(convertResponseToString(response).getBytes());
        samlResponseEncoded = new String(responseEncoded);
    }

    private String convertResponseToString(Response response) throws MarshallingException {
        ResponseMarshaller marshaller = new ResponseMarshaller();
        Element plaintextElement = marshaller.marshall(response);
        return XMLHelper.nodeToString(plaintextElement);
    }

}
