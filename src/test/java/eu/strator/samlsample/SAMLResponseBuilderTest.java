package eu.strator.samlsample;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-saml-context.xml"})
public class SAMLResponseBuilderTest {

    @Autowired 
    private SAMLResponseBuilderSample samlResponseBuilder;

    private Element responseElement;

    @Test
    public void issuer_element_is_mandatory_in_saml_response() throws Exception {
        when_we_build_a_saml_response();
        then_response_contains_an_issuer_element();
    }

    @Test
    public void status_should_be_success_in_saml_response() throws Exception {
        when_we_build_a_saml_response();
        then_response_has_a_success_status();
    }

    @Test
    public void assertion_element_is_mandatory_in_saml_response() throws Exception {
        when_we_build_a_saml_response();
        then_response_contains_an_assertion_element();
    }

    @Test
    public void issuer_element_is_mandatory_in_assertion_element() throws Exception {
        when_we_build_a_saml_response();
        then_assertion_contains_an_issuer_element();
    }

    @Test
    public void subject_element_is_mandatory_in_assertion_element() throws Exception {
        when_we_build_a_saml_response();
        then_assertion_contains_a_subject_element();
    }

    @Test
    public void nameId_element_is_mandatory_in_subject_element() throws Exception {
        when_we_build_a_saml_response();
        then_subject_contains_a_name_id_element();
    }

    @Test
    public void data_element_is_mandatory_in_subject_confirmation_element() throws Exception {
        when_we_build_a_saml_response();
        then_subject_confirmation_contains_data_element();
        then_confirmation_data_element_has_attribute(SubjectConfirmationData.NOT_ON_OR_AFTER_ATTRIB_NAME);
        then_confirmation_data_element_has_attribute(SubjectConfirmationData.RECIPIENT_ATTRIB_NAME);
    }

    @Test
    public void subject_confirmation_element_is_mandatory_in_subject_element() throws Exception {
        when_we_build_a_saml_response();
        then_subject_contains_a_subject_confirmation_element();
    }

    @Test
    public void conditions_element_is_mandatory_in_assertion_element() throws Exception {
        when_we_build_a_saml_response();
        then_assertions_contains_conditions_element();
        then_conditions_element_has_attribute(Conditions.NOT_ON_OR_AFTER_ATTRIB_NAME);
    }

    @Test
    public void audienceRestriction_element_is_mandatory_in_conditions_element() throws Exception {
        when_we_build_a_saml_response();
        then_conditions_contains_AudienceRestriction_element();
    }

    @Test
    public void audience_element_is_mandatory_in_AudienceRestriction_element() throws Exception {
        when_we_build_a_saml_response();
        then_AudienceRestriction_contains_Audience_element();
    }

    @Test
    public void attributeStatement_element_is_mandatory_in_AudienceRestriction_element() throws Exception {
        when_we_build_a_saml_response();
        then_assertions_contains_attributeStatement_element();
    }

    @Test
    public void attribute_element_is_mandatory_in_attributeStatement_element() throws Exception {
        when_we_build_a_saml_response();
        then_attributeStatement_element_contains_attribute_element();
    }

    @Test
    public void attributevalue_element_is_mandatory_in_attribute_element() throws Exception {
        when_we_build_a_saml_response();
        then_attribute_element_contains_attribute_value_element();
    }

    @Test
    public void authnStatement_element_is_mandatory_in_assertion_element() throws Exception {
        when_we_build_a_saml_response();
        then_assertions_contains_authnStatement_element();
    }

    @Test
    public void authnContext_element_is_mandatory_in_authnStatement_element() throws Exception {
        when_we_build_a_saml_response();
        then_authnStatement_element_contains_authnContext_element();
    }

    @Test
    public void authnContextClassRef_element_is_mandatory_in_authnContext_element() throws Exception {
        when_we_build_a_saml_response();
        then_authnContext_element_contains_authnContextClassRef_element();
    }

    @Test
    public void authnContextClassRef_value_must_be_PasswordProtectedTransport() throws Exception {
        when_we_build_a_saml_response();
        then_authnContextClassRef_value_is_PasswordProtectedTransport();
    }

    private void when_we_build_a_saml_response() {
        Response response = samlResponseBuilder.buildResponse();
        ResponseMarshaller marshaller = new ResponseMarshaller();
        try {
            responseElement = marshaller.marshall(response);
        } catch (MarshallingException e) {
            e.printStackTrace();
        }
    }

    private void then_assertions_contains_attributeStatement_element() {
        assertionContains(AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_attributeStatement_element_contains_attribute_element() {
        Node assertionNode = getAssertionNode();
        Node attributeStatement = getNode(AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node attribute = getNode(Attribute.DEFAULT_ELEMENT_LOCAL_NAME, attributeStatement.getChildNodes());
        Assertions.assertThat(attribute).isNotNull();
    }

    private void then_attribute_element_contains_attribute_value_element() {
        Node assertionNode = getAssertionNode();
        Node attributeStatement = getNode(AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node attribute = getNode(Attribute.DEFAULT_ELEMENT_LOCAL_NAME, attributeStatement.getChildNodes());
        Node attributeValue = getNode(AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, attribute.getChildNodes());
        Assertions.assertThat(attributeValue).isNotNull();
    }

    private void then_assertions_contains_authnStatement_element() {
        assertionContains(AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_authnStatement_element_contains_authnContext_element() {
        Node assertionNode = getAssertionNode();
        Node authnStatement = getNode(AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node authnContext = getNode(AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, authnStatement.getChildNodes());
        Assertions.assertThat(authnContext).isNotNull();
    }

    private void then_authnContext_element_contains_authnContextClassRef_element() {
        Node assertionNode = getAssertionNode();
        Node authnStatement = getNode(AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node authnContext = getNode(AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, authnStatement.getChildNodes());
        Node authnContextClassRef = getNode(AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME, authnContext.getChildNodes());
        Assertions.assertThat(authnContextClassRef).isNotNull();
    }

    private void then_authnContextClassRef_value_is_PasswordProtectedTransport() {
        Node assertionNode = getAssertionNode();
        Node authnStatement = getNode(AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node authnContext = getNode(AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, authnStatement.getChildNodes());
        Node authnContextClassRef = getNode(AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME, authnContext.getChildNodes());
        Assertions.assertThat(authnContextClassRef.getTextContent()).isEqualTo(AuthnContext.PPT_AUTHN_CTX);
    }

    private void then_AudienceRestriction_contains_Audience_element() {
        Node assertionNode = getAssertionNode();
        Node conditions = getNode(Conditions.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node audienceRestriction = getNode(AudienceRestriction.DEFAULT_ELEMENT_LOCAL_NAME, conditions.getChildNodes());
        Node audience = getNode(Audience.DEFAULT_ELEMENT_LOCAL_NAME, audienceRestriction.getChildNodes());
        Assertions.assertThat(audience).isNotNull();
    }


    private void then_conditions_contains_AudienceRestriction_element() {
        Node assertionNode = getAssertionNode();
        Node conditions = getNode(Conditions.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());
        Node audienceRestriction = getNode(AudienceRestriction.DEFAULT_ELEMENT_LOCAL_NAME, conditions.getChildNodes());
        Assertions.assertThat(audienceRestriction).isNotNull();
    }

    private void then_assertions_contains_conditions_element() {
        assertionContains(Conditions.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_subject_confirmation_contains_data_element() {
        Node subject = getSubjectNode();
        Node subjectConfirmation = getNode(SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME, subject.getChildNodes());
        Node data = getNode(SubjectConfirmationData.DEFAULT_ELEMENT_LOCAL_NAME, subjectConfirmation.getChildNodes());
        Assertions.assertThat(data).isNotNull();
    }

    private void then_confirmation_data_element_has_attribute(String attribName) {
        Node subject = getSubjectNode();
        Node subjectConfirmation = getNode(SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME, subject.getChildNodes());
        Node data = getNode(SubjectConfirmationData.DEFAULT_ELEMENT_LOCAL_NAME, subjectConfirmation.getChildNodes());

        elementHasAttribute(data, attribName);
    }

    private void then_conditions_element_has_attribute(String attribName) {
        Node assertionNode = getAssertionNode();
        Node conditions = getNode(Conditions.DEFAULT_ELEMENT_LOCAL_NAME, assertionNode.getChildNodes());

        elementHasAttribute(conditions, attribName);
    }

    private void then_subject_contains_a_subject_confirmation_element() {
        subjectContains(SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_subject_contains_a_name_id_element() {
        subjectContains(NameID.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_assertion_contains_a_subject_element() {
        assertionContains(Subject.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_assertion_contains_an_issuer_element() {
        assertionContains(Issuer.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_response_contains_an_assertion_element() {
        responseContains(Assertion.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void elementHasAttribute(Node node, String attribName) {
        NamedNodeMap attributes = node.getAttributes();
        boolean findNotOnOrAfterAttribute = false;
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            if (item.getLocalName().equals(attribName))
                findNotOnOrAfterAttribute = true;
        }

        Assertions.assertThat(findNotOnOrAfterAttribute).isTrue();
    }

    private void then_response_contains_an_issuer_element() {
        responseContains(Issuer.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void then_response_has_a_success_status() {
        responseContains(Status.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    private void responseContains(String localName) {
        Assertions.assertThat(getNode(localName, responseElement.getChildNodes())).isNotNull();
    }

    private void assertionContains(String localName) {
        Node assertion = getAssertionNode();
        Assertions.assertThat(getNode(localName, assertion.getChildNodes())).isNotNull();
    }

    private void subjectContains(String localName) {
        Node subject = getSubjectNode();
        Assertions.assertThat(getNode(localName, subject.getChildNodes())).isNotNull();
    }

    private Node getSubjectNode() {
        Node assertion = getAssertionNode();
        return getNode(Subject.DEFAULT_ELEMENT_LOCAL_NAME, assertion.getChildNodes());
    }

    private Node getAssertionNode() {
        return getNode(Assertion.DEFAULT_ELEMENT_LOCAL_NAME, responseElement.getChildNodes());
    }

    private Node getNode(String localName, NodeList nodes) {
        for ( int x = 0; x < nodes.getLength(); x++ ) {
            Node node = nodes.item(x);
            if (node.getLocalName().equalsIgnoreCase(localName)) {
                return node;
            }
        }
        return null;
    }

}