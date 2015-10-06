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
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.UUID;

@Component
public class SAMLResponseBuilderSample {

    private String issuerValue = "http://idp.test.tpos.logista.com";

    private String destination="http://testcfs.logista.com/adfs/services/trust";

    public Response buildResponse() {

        Response response = null;
        try {
            DefaultBootstrap.bootstrap();

            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

            Assertion assertion = buildAssertion(builderFactory);

            SAMLObjectBuilder statusCodeBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
            StatusCode statusCode = (StatusCode) statusCodeBuilder.buildObject();
            statusCode.setValue(StatusCode.SUCCESS_URI);

            SAMLObjectBuilder statusBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
            Status status = (Status) statusBuilder.buildObject();
            status.setStatusCode(statusCode);

            // Create the Response
            SAMLObjectBuilder reponseBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
            response = (Response)reponseBuilder.buildObject();
            response.getAssertions().add(assertion);
            response.setDestination(destination);
            response.setIssuer(buildIssuer(builderFactory));
            response.setIssueInstant(new DateTime());
            response.setStatus(status);

            String newID = "_" + UUID.randomUUID().toString();
            response.setID(newID);
            logResponse(response);

        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (MarshallingException e) {
            e.printStackTrace();
        }
        return response;
    }

    public Assertion buildAssertion(XMLObjectBuilderFactory builderFactory) throws MarshallingException {

        // Create the NameIdentifier
        SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
        NameID nameId = (NameID) nameIdBuilder.buildObject();
        nameId.setValue("E0000019512");
        nameId.setFormat(NameID.UNSPECIFIED);

        // Create the Subject
        SAMLObjectBuilder subjectConfirmationDataBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        SubjectConfirmationData subjectConfirmationData = (SubjectConfirmationData) subjectConfirmationDataBuilder.buildObject();
        subjectConfirmationData.setRecipient("https://testcfs.logista.com/adfs/ls/");
        subjectConfirmationData.setNotOnOrAfter((new LocalDateTime()).plusHours(1).toDateTime());

        SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder.buildObject();
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
        Subject subject = (Subject) subjectBuilder.buildObject();
        subject.setNameID(nameId);
        subject.getSubjectConfirmations().add(subjectConfirmation);

        SAMLObjectBuilder conditionBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
        Conditions conditions = (Conditions) conditionBuilder.buildObject();

        SAMLObjectBuilder audienceRestrictionBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        AudienceRestriction audienceRestriction = (AudienceRestriction) audienceRestrictionBuilder.buildObject();

        SAMLObjectBuilder audienceBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Audience.DEFAULT_ELEMENT_NAME);
        Audience audience = (Audience) audienceBuilder.buildObject();
        audience.setAudienceURI("https://testcfs.logista.com");

        audienceRestriction.getAudiences().add(audience);
        conditions.setNotOnOrAfter((new LocalDateTime()).plusHours(1).toDateTime());
        conditions.getAudienceRestrictions().add(audienceRestriction);

        SAMLObjectBuilder statementBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement statement = (AttributeStatement)statementBuilder.buildObject();
        SAMLObjectBuilder attributeBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        Attribute attribute = (Attribute) attributeBuilder.buildObject();
        attribute.setName("uid");
        attribute.setNameFormat(Attribute.BASIC);

        XSStringBuilder stringBuilder = (XSStringBuilder) builderFactory.getBuilder(XSString.TYPE_NAME);
        XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue("E0000019512");
        attribute.getAttributeValues().add(attributeValue);

        statement.getAttributes().add(attribute);

        // Create the assertion
        SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        Assertion assertion = (Assertion) assertionBuilder.buildObject();
        assertion.setIssuer(buildIssuer(builderFactory));
        assertion.setIssueInstant(new LocalDateTime().toDateTime());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setSubject(subject);
        assertion.setConditions(conditions);
        assertion.getAttributeStatements().add(statement);
        assertion.setID("_" + UUID.randomUUID().toString());

        return assertion;
    }

    private void logResponse(Response response) throws MarshallingException {
        ResponseMarshaller marshaller = new ResponseMarshaller();
        Element plaintextElement = marshaller.marshall(response);
        System.out.println("##############################SAMLResponse########################################");
        System.out.println(XMLHelper.nodeToString(plaintextElement));
        System.out.println("##################################################################################");
    }

    private Issuer buildIssuer(XMLObjectBuilderFactory builderFactory) {
        // Create Issuer
        SAMLObjectBuilder issuerBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = (Issuer) issuerBuilder.buildObject();
        issuer.setValue(issuerValue);
        return issuer;
    }




}
