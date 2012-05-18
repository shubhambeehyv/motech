package org.motechproject.sms.http.template;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Ignore;
import org.junit.Test;
import org.motechproject.sms.http.TemplateReader;
import org.motechproject.sms.http.domain.HttpMethodType;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class SmsHttpTemplateTest {

    @Test
    public void shouldGenerateRequestUrl() throws URIException {
        Request request = new Request();
        request.setUrlPath("http://smshost.com/sms/send");
        Map<String, String> queryParameters = new HashMap<String, String>() {{
            put("to", "foobar");
        }};
        request.setQueryParameters(queryParameters);

        SmsHttpTemplate smsHttpTemplate = createSmsHttpTemplate(request);

        HttpMethod httpMethod = smsHttpTemplate.generateRequestFor(Arrays.asList("123"), "some Message");
        assertEquals("http://smshost.com/sms/send?to=foobar", httpMethod.getURI().getURI());
    }

    @Test
    public void shouldReplaceMessageVariableWithValue() throws URIException {
        Request request = new Request();
        request.setUrlPath("http://smshost.com/sms/send");
        Map<String, String> queryParameters = new HashMap<String, String>() {{
            put("message", "$message");
        }};
        request.setQueryParameters(queryParameters);

        SmsHttpTemplate smsHttpTemplate = createSmsHttpTemplate(request);

        HttpMethod httpMethod = smsHttpTemplate.generateRequestFor(Arrays.asList("123"), "foobar");
        assertEquals("http://smshost.com/sms/send?message=foobar", httpMethod.getURI().getURI());
    }

    @Test
    public void shouldReplaceReciepientsVariableWithValue() throws URIException {
        Request request = new Request();
        request.setUrlPath("http://smshost.com/sms/send");
        request.setRecipientsSeparator(",");
        Map<String, String> queryParameters = new HashMap<String, String>() {{
            put("recipients", "$recipients");
        }};
        request.setQueryParameters(queryParameters);

        SmsHttpTemplate smsHttpTemplate = createSmsHttpTemplate(request);

        HttpMethod httpMethod = smsHttpTemplate.generateRequestFor(Arrays.asList("123", "456", "789"), "some message");
        assertEquals("http://smshost.com/sms/send?recipients=123,456,789", httpMethod.getURI().getURI());
    }

    @Test
    public void shouldCreateCorrectRequestTypeBasedOnConfiguration() {
        TemplateReader templateReader = new TemplateReader();
        SmsHttpTemplate smsHttpPOSTTemplate = templateReader.getTemplate("/templates/sms-http-post-template.json");
        assertEquals(PostMethod.class, smsHttpPOSTTemplate.generateRequestFor(Arrays.asList("123", "456", "789"),
                "Hello World").getClass());

        SmsHttpTemplate smsHttpGETTemplate = templateReader.getTemplate("/templates/sms-http-get-template.json");
        assertEquals(GetMethod.class, smsHttpGETTemplate.generateRequestFor(Arrays.asList("123", "456", "789"),
                "Hello World").getClass());
    }

    @Test
    public void shouldSetBodyParametersForPOSTRequestType() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        params.put("recipients", "$recipients");
        params.put("message", "$message");
        Request request = new SmsSendTemplateBuilder.RequestBuilder()
                .withType(HttpMethodType.POST)
                .withBodyParameters(params)
                .withRecipientSeperator(",")
                .build();

        SmsHttpTemplate smsHttpTemplate = createSmsHttpTemplate(request);

        PostMethod httpMethod = (PostMethod) smsHttpTemplate.generateRequestFor(Arrays.asList("123", "456", "789"), "someMessage");
        assertEquals(4, httpMethod.getParameters().length);
        assertEquals("value1", httpMethod.getParameter("key1").getValue());
        assertEquals("value2", httpMethod.getParameter("key2").getValue());
        assertEquals("123,456,789", httpMethod.getParameter("recipients").getValue());
        assertEquals("someMessage", httpMethod.getParameter("message").getValue());
    }

    @Test
//    @Ignore
    public void shouldSendSmsUsingHttpPost() throws IOException {
        byte[] encodedPassword = ("sanchit.bahal" + ":" + "spleen.african87").getBytes();
        BASE64Encoder encoder = new BASE64Encoder();

        String url = "http://api.messaging.staging.voxeo.net/1.0/messaging";
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestHeader("Authorization", "Basic " + encoder.encode(encodedPassword));
        postMethod.setParameter("botkey", "362374");
        postMethod.setParameter("apimethod", "send");
        postMethod.setParameter("msg", "Hello Platform World");
        postMethod.setParameter("user", "017732345337");
        postMethod.setParameter("network", "SMS");
        postMethod.setParameter("from", "4159062932");
        HttpClient httpClient = new HttpClient();
        int status = httpClient.executeMethod(postMethod);
        String responseBodyAsString = postMethod.getResponseBodyAsString();
        System.out.println(String.format("Status:%s; Response:%s", status, responseBodyAsString));
    }

    private SmsHttpTemplate createSmsHttpTemplate(Request request) {
        SmsHttpTemplate smsHttpTemplate = new SmsHttpTemplate();
        Outgoing outgoing = new Outgoing();
        outgoing.setRequest(request);
        smsHttpTemplate.setOutgoing(outgoing);
        return smsHttpTemplate;
    }
}
