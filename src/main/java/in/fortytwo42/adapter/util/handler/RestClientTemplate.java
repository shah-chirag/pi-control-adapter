//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package in.fortytwo42.adapter.util.handler;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.itzmeds.rac.core.client.ClientTemplate;
import com.itzmeds.rac.core.client.ServiceClientUtils;

public class RestClientTemplate implements ClientTemplate<Response> {
    private WebTarget restServiceTarget = null;

    public RestClientTemplate(WebTarget restServiceTarget) {
        this.restServiceTarget = restServiceTarget;
    }

    public <M> Response retrieve(Map<String, M> queryParams, Map<String, String> pathParams) {
        Invocation.Builder requestBuilder = this.getRequestBuilder(queryParams, pathParams);
        return requestBuilder.get();
    }

    public <M> Response delete(Map<String, M> queryParams, Map<String, String> pathParams) {
        Invocation.Builder requestBuilder = this.getRequestBuilder(queryParams, pathParams);
        return requestBuilder.delete();
    }

    public <M, E> Response create(Map<String, M> queryParams, Map<String, String> pathParams, Entity<E> requestEntity) {
        Invocation.Builder requestBuilder = this.getRequestBuilder(queryParams, pathParams);
        return requestBuilder.post(requestEntity);
    }

    public <M, E> Response update(Map<String, M> queryParams, Map<String, String> pathParams, Entity<E> requestEntity) {
        Invocation.Builder requestBuilder = this.getRequestBuilder(queryParams, pathParams);
        return requestBuilder.put(requestEntity);
    }

    public <M> Response retrieve(Map<String, M> queryParams, Map<String, String> pathParams, String basicAuthUid, String basicAuthPwd) {
        return this.getRequestBuilder(queryParams, pathParams, basicAuthUid, basicAuthPwd).get();
    }

    public <M> Response delete(Map<String, M> queryParams, Map<String, String> pathParams, String basicAuthUid, String basicAuthPwd) {
        return this.getRequestBuilder(queryParams, pathParams, basicAuthUid, basicAuthPwd).delete();
    }

    public <M, E> Response create(Map<String, M> queryParams, Map<String, String> pathParams, Entity<E> requestEntity, String basicAuthUid, String basicAuthPwd) {
        return this.getRequestBuilder(queryParams, pathParams, basicAuthUid, basicAuthPwd).post(requestEntity);
    }

    public <M, E> Response update(Map<String, M> queryParams, Map<String, String> pathParams, Entity<E> requestEntity, String basicAuthUid, String basicAuthPwd) {
        return this.getRequestBuilder(queryParams, pathParams, basicAuthUid, basicAuthPwd).put(requestEntity);
    }

    public <M, E> Response create(Map<String, M> queryParams, Map<String, String> pathParams, Entity<E> requestEntity, String accessToken) {
        return this.getRequestBuilder(queryParams, pathParams, accessToken).post(requestEntity);
    }

    public <M> Response retrieve(Map<String, M> queryParams, Map<String, String> pathParams, String accessToken) {
        return this.getRequestBuilder(queryParams, pathParams, accessToken).get();
    }

    public <M, E> Response update(Map<String, M> queryParams, Map<String, String> pathParams, Entity<E> requestEntity, String accessToken) {
        return this.getRequestBuilder(queryParams, pathParams, accessToken).put(requestEntity);
    }

    public <M> Response delete(Map<String, M> queryParams, Map<String, String> pathParams, String accessToken) {
        return this.getRequestBuilder(queryParams, pathParams, accessToken).delete();
    }

    private <M> Invocation.Builder getRequestBuilder(Map<String, M> queryParams, Map<String, String> pathParams) {
        WebTarget viewTarget = null;
        Map<String, String> pathParams1 = pathParams == null ? new HashMap() : pathParams;
        Map<String, M> queryParams1 = queryParams == null ? new HashMap() : queryParams;
        viewTarget = ServiceClientUtils.addPathParameters((Map)pathParams1, this.restServiceTarget);
        viewTarget = ServiceClientUtils.addQueryParameters((Map)queryParams1, viewTarget);
        return viewTarget.request();
    }

    private <M> Invocation.Builder getRequestBuilder(Map<String, M> queryParams, Map<String, String> pathParams, String basicAuthUid, String basicAuthPwd) {
        Invocation.Builder reqBuilder = this.getRequestBuilder(queryParams, pathParams);
        if (basicAuthUid != null) {
            reqBuilder = reqBuilder.property("jersey.config.client.http.auth.basic.username", basicAuthUid);
        }

        if (basicAuthPwd != null) {
            reqBuilder = reqBuilder.property("jersey.config.client.http.auth.basic.password", basicAuthPwd);
        }

        return reqBuilder;
    }

    private <M> Invocation.Builder getRequestBuilder(Map<String, M> queryParams, Map<String, String> pathParams, String accessToken) {
        Invocation.Builder reqBuilder = this.getRequestBuilder(queryParams, pathParams);
        return reqBuilder.property("jersey.config.client.oauth2.access.token", accessToken);
    }
}
