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

import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.stream.TransformableRequestStreamBuilder;
import io.gravitee.gateway.api.http.stream.TransformableResponseStreamBuilder;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.exception.TransformationException;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponseContent;
import io.gravitee.policy.xml2json.configuration.PolicyScope;
import io.gravitee.policy.xml2json.configuration.XmlToJsonTransformationPolicyConfiguration;
import io.gravitee.policy.xml2json.transformer.XML;

import java.util.function.Function;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class XmlToJsonTransformationPolicy {

    private final static String APPLICATION_JSON = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    /**
     * XML to Json transformation configuration
     */
    private final XmlToJsonTransformationPolicyConfiguration xmlToJsonTransformationPolicyConfiguration;

    public XmlToJsonTransformationPolicy(final XmlToJsonTransformationPolicyConfiguration xmlToJsonTransformationPolicyConfiguration) {
        this.xmlToJsonTransformationPolicyConfiguration = xmlToJsonTransformationPolicyConfiguration;
    }

    @OnResponseContent
    public ReadWriteStream onResponseContent(Response response) {
        if (xmlToJsonTransformationPolicyConfiguration.getScope() == null || xmlToJsonTransformationPolicyConfiguration.getScope() == PolicyScope.RESPONSE) {
            return TransformableResponseStreamBuilder
                    .on(response)
                    .contentType(APPLICATION_JSON)
                    .transform(map())
                    .build();
        }

        return null;
    }

    @OnRequestContent
    public ReadWriteStream onRequestContent(Request request) {
        if (xmlToJsonTransformationPolicyConfiguration.getScope() == PolicyScope.REQUEST) {
            return TransformableRequestStreamBuilder
                    .on(request)
                    .contentType(APPLICATION_JSON)
                    .transform(map())
                    .build();
        }

        return null;
    }

    Function<Buffer, Buffer> map() {
        return input -> {
            try {
                return Buffer.buffer(XML.toJSONObject(input.toString()).toString());
            } catch (Exception ex) {
                throw new TransformationException("Unable to transform XML into JSON: " + ex.getMessage(), ex);
            }
        };
    }
}
