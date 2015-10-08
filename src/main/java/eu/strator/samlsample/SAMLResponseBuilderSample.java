package eu.strator.samlsample;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import java.util.UUID;

@Component
public class SAMLResponseBuilderSample {

    private String issuerValue = "http://idp.test.tpos.logista.com";
    private String destination = "http://testcfs.logista.com/adfs/services/trust";

    @PostConstruct
    private void init() {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Response buildResponse() {
        Response response = getSamlObject(Response.DEFAULT_ELEMENT_NAME, Response.class);
        response.setID(generateId());
        response.setDestination(destination);
        response.setIssueInstant(now());
        response.setIssuer(buildIssuer());
        response.getAssertions().add(buildAssertion());
        response.setStatus(buildStatusElement());
        logResponse(response);

        return response;
    }

    private DateTime now() {
        return new LocalDateTime().toDateTime();
    }

    private String generateId() {
        return "_" + UUID.randomUUID().toString();
    }

    private Status buildStatusElement() {
        StatusCode statusCode = getSamlObject(StatusCode.DEFAULT_ELEMENT_NAME, StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);

        Status status = getSamlObject(Status.DEFAULT_ELEMENT_NAME, Status.class);
        status.setStatusCode(statusCode);
        return status;
    }

    private Assertion buildAssertion() {
        Assertion assertion = getSamlObject(Assertion.DEFAULT_ELEMENT_NAME, Assertion.class);
        assertion.setID(generateId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssueInstant(now());
        assertion.setIssuer(buildIssuer());
        assertion.setSubject(buildSubjectElement());
        assertion.setConditions(buildConditionsElement());
        assertion.getAttributeStatements().add(buildAttributeStatementElement());
        assertion.getAuthnStatements().add(buildAuthnStatementElement());

        return assertion;
    }

    private AuthnStatement buildAuthnStatementElement() {
        AuthnContextClassRef authnContextClassRef = getSamlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME, AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);

        AuthnContext authnContext = getSamlObject(AuthnContext.DEFAULT_ELEMENT_NAME, AuthnContext.class);
        authnContext.setAuthnContextClassRef(authnContextClassRef);

        AuthnStatement authnStatement = getSamlObject(AuthnStatement.DEFAULT_ELEMENT_NAME, AuthnStatement.class);
        authnStatement.setAuthnInstant(now());
        authnStatement.setAuthnContext(authnContext);

        return authnStatement;
    }

    private AttributeStatement buildAttributeStatementElement() {
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue("E0000013570");

        Attribute attribute = getSamlObject(Attribute.DEFAULT_ELEMENT_NAME, Attribute.class);
        attribute.setName("uid");
        attribute.setNameFormat(Attribute.BASIC);
        attribute.getAttributeValues().add(attributeValue);

        AttributeStatement statement = getSamlObject(AttributeStatement.DEFAULT_ELEMENT_NAME, AttributeStatement.class);
        statement.getAttributes().add(attribute);
        return statement;
    }

    private Conditions buildConditionsElement() {
        Audience audience = getSamlObject(Audience.DEFAULT_ELEMENT_NAME, Audience.class);
        audience.setAudienceURI("https://testcfs.logista.com");

        AudienceRestriction audienceRestriction = getSamlObject(AudienceRestriction.DEFAULT_ELEMENT_NAME, AudienceRestriction.class);
        audienceRestriction.getAudiences().add(audience);

        Conditions conditions = getSamlObject(Conditions.DEFAULT_ELEMENT_NAME, Conditions.class);
        conditions.setNotOnOrAfter(now().plusHours(1));
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private Subject buildSubjectElement() {
        Subject subject = getSamlObject(Subject.DEFAULT_ELEMENT_NAME, Subject.class);
        subject.setNameID(buildNameIdElement());
        subject.getSubjectConfirmations().add(buildSubjectConfirmationElement());
        return subject;
    }

    private SubjectConfirmation buildSubjectConfirmationElement() {
        SubjectConfirmation subjectConfirmation = getSamlObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME, SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(buildSubjectConfirmationDataElement());
        return subjectConfirmation;
    }

    private SubjectConfirmationData buildSubjectConfirmationDataElement() {
        SubjectConfirmationData subjectConfirmationData = getSamlObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME, SubjectConfirmationData.class);
        subjectConfirmationData.setRecipient("https://testcfs.logista.com/adfs/ls/");
        subjectConfirmationData.setNotOnOrAfter(now().plusHours(1));
        return subjectConfirmationData;
    }

    private NameID buildNameIdElement() {
        NameID nameId = getSamlObject(NameID.DEFAULT_ELEMENT_NAME, NameID.class);
        nameId.setValue("E0000013570");
        nameId.setFormat(NameID.UNSPECIFIED);
        return nameId;
    }

    private Issuer buildIssuer() {
        Issuer issuer = getSamlObject(Issuer.DEFAULT_ELEMENT_NAME, Issuer.class);
        issuer.setValue(issuerValue);
        return issuer;
    }

    private void logResponse(Response response) {
        try {
            ResponseMarshaller marshaller = new ResponseMarshaller();
            Element plaintextElement = marshaller.marshall(response);
            System.out.println(XMLHelper.nodeToString(plaintextElement));
        } catch (MarshallingException e) {
            e.printStackTrace();
        }
    }

    private <T> T getSamlObject(QName elementName, Class<T> clazz) {
        SAMLObjectBuilder builder = (SAMLObjectBuilder) Configuration.getBuilderFactory().getBuilder(elementName);
        return (T) builder.buildObject();
    }
}
