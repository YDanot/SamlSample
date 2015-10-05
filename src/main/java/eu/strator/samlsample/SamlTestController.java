package eu.strator.samlsample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/*")
public class SamlTestController {

    @Autowired
    private CommandeLogistadisValidationBeanSample commandeLogistadisValidationBean;

    @RequestMapping("")
    @ResponseBody
    String home() {
        return "        <form id=\"Idp-Initated\" method=\"post\" action=\"https://testcfs.logista.com/adfs/ls/idpinitiatedsignon.aspx\">\n" +
                "            <input type=\"hidden\" name=\"SAMLResponse\"\n" +
                "                   value=\""+commandeLogistadisValidationBean.getSamlResponseEncoded()+"\"/>\n" +
                "            <input type=\"hidden\" name=\"relaystate\"\n" +
                "                   value=\""+commandeLogistadisValidationBean.getSamlRelayStateEncoded()+"\"/>\n" +
                "            <input type=\"submit\" value=\"Submit\"/>\n" +
                "        </form>";
    }
}