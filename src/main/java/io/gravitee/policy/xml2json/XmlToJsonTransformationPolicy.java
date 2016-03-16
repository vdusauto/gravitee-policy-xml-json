/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.xml2json;

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpHeadersValues;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.BufferedReadWriteStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.SimpleReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import io.gravitee.policy.xml2json.transformer.XML;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class XmlToJsonTransformationPolicy {

    @OnResponse
    public void onResponse(Request request, Response response, PolicyChain policyChain) {
        policyChain.doNext(request, response);
    }

    @OnResponseContent
    public ReadWriteStream onResponseContent(Response response) {
        return new BufferedReadWriteStream() {
            StringBuffer buffer = new StringBuffer();

            @Override
            public SimpleReadWriteStream<Buffer> write(Buffer chunk) {
                buffer.append(chunk.toString());
                return this;
            }

            @Override
            public void end() {
                String content;

                try {
                    content = XML.toJSONObject(buffer.toString()).toString();
                    response.headers().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                } catch (Exception ex) {
                    content = ex.getMessage();

                    response.status(HttpStatusCode.INTERNAL_SERVER_ERROR_500);
                    response.headers().set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
                    response.headers().set(HttpHeaders.CONNECTION, HttpHeadersValues.CONNECTION_CLOSE);
                }

                response.headers().remove(HttpHeaders.TRANSFER_ENCODING);
                response.headers().set(HttpHeaders.CONTENT_LENGTH, Integer.toString(content.length()));

                super.write(Buffer.buffer(content));
                super.end();
            }
        };
    }
}