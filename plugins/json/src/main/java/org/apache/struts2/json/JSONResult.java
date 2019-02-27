/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.json.smd.SMDGenerator;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.WildcardUtil;

/**
 * <!-- START SNIPPET: description -->
 * <p>
 * This result serializes an action into JSON.
 * </p>
 * <!-- END SNIPPET: description -->
 * <p><u>Result parameters:</u></p>
 * <!-- START SNIPPET: parameters -->
 * <ul>
 *
 * <li>excludeProperties - list of regular expressions matching the properties
 * to be excluded. The regular expressions are evaluated against the OGNL
 * expression representation of the properties. </li>
 *
 * </ul>
 * <!-- END SNIPPET: parameters -->
 * <p><b>Example:</b></p>
 *
 * <pre>
 * &lt;!-- START SNIPPET: example --&gt;
 * &lt;result name=&quot;success&quot; type=&quot;json&quot; /&gt;
 * &lt;!-- END SNIPPET: example --&gt;
 * </pre>
 */
public class JSONResult implements Result {

    private static final long serialVersionUID = 8624350183189931165L;

    private static final Logger LOG = LogManager.getLogger(JSONResult.class);

    private String encoding;
    private String defaultEncoding = "UTF-8";
    private List<Pattern> includeProperties;
    private List<Pattern> excludeProperties;
    private String root;
    private boolean wrapWithComments;
    private boolean prefix;
    private boolean enableSMD = false;
    private boolean enableGZIP = false;
    private boolean ignoreHierarchy = true;
    private boolean ignoreInterfaces = true;
    private boolean enumAsBean = JSONWriter.ENUM_AS_BEAN_DEFAULT;
    private boolean noCache = false;
    private boolean cacheBeanInfo = true;
    private boolean excludeNullProperties = false;
    private String defaultDateFormat = null;
    private int statusCode;
    private int errorCode;
    private String callbackParameter;
    private String contentType;
    private String wrapPrefix;
    private String wrapSuffix;
    private boolean devMode = false;
    private JSONUtil jsonUtil;
    
    @Inject(StrutsConstants.STRUTS_I18N_ENCODING)
    void setDefaultEncoding(String val) {
        this.defaultEncoding = val;
    }

    @Inject
    void setJsonUtil(JSONUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    /**
     * Sets a comma-delimited list of regular expressions to match properties
     * that should be included in the JSON output.
     *
     * @param commaDelim A comma-delimited list of regular expressions
     */
    void setIncludeProperties(String commaDelim) {
        includeProperties = JSONUtil.processIncludePatterns(JSONUtil.asSet(commaDelim), JSONUtil.REGEXP_PATTERN);
    }

    public void execute(ActionInvocation invocation) throws Exception {
        ActionContext actionContext = invocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) actionContext.get(StrutsStatics.HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) actionContext.get(StrutsStatics.HTTP_RESPONSE);
        
        // only permit caching bean information when struts devMode = false
        cacheBeanInfo = !devMode;
        
        try {
            Object rootObject;
            rootObject = readRootObject(invocation);
            writeToResponse(response, createJSONString(request, rootObject), enableGzip(request));
        } catch (IOException exception) {
            LOG.error(exception.getMessage(), exception);
            throw exception;
        }
    }

    private Object readRootObject(ActionInvocation invocation) {
        if (enableSMD) {
            return buildSMDObject(invocation);
        }
        return findRootObject(invocation);
    }

    private Object findRootObject(ActionInvocation invocation) {
        Object rootObject;
        if (this.root != null) {
            ValueStack stack = invocation.getStack();
            rootObject = stack.findValue(root);
        } else {
            rootObject = invocation.getStack().peek(); // model overrides action
        }
        return rootObject;
    }

    private String createJSONString(HttpServletRequest request, Object rootObject) throws JSONException {
        String json = jsonUtil.serialize(rootObject, excludeProperties, includeProperties, ignoreHierarchy,
                                         enumAsBean, excludeNullProperties, defaultDateFormat, cacheBeanInfo);
        json = addCallbackIfApplicable(request, json);
        return json;
    }

    private boolean enableGzip(HttpServletRequest request) {
        return enableGZIP && JSONUtil.isGzipInRequest(request);
    }

    private void writeToResponse(HttpServletResponse response, String json, boolean gzip) throws IOException {
        JSONUtil.writeJSONToResponse(new SerializationParams(response, getEncoding(), isWrapWithComments(),
                json, false, gzip, noCache, statusCode, errorCode, prefix, contentType, wrapPrefix,
                wrapSuffix));
    }

    @SuppressWarnings("unchecked")
    private org.apache.struts2.json.smd.SMD buildSMDObject(ActionInvocation invocation) {
        return new SMDGenerator(findRootObject(invocation), excludeProperties, ignoreInterfaces).generate(invocation);
    }

    /**
     * Retrieve the encoding
     *
     * @return The encoding associated with this template (defaults to the value
     *         of param 'encoding', if empty default to 'struts.i18n.encoding' property)
     */
    protected String getEncoding() {
        String encoding = this.encoding;

        if (encoding == null) {
            encoding = this.defaultEncoding;
        }

        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }

        if (encoding == null) {
            encoding = "UTF-8";
        }

        return encoding;
    }

    private String addCallbackIfApplicable(HttpServletRequest request, String json) {
        if ((callbackParameter != null) && (callbackParameter.length() > 0)) {
            String callbackName = request.getParameter(callbackParameter);
            if (StringUtils.isNotEmpty(callbackName)) {
                json = callbackName + "(" + json + ")";
            }
        }
        return json;
    }

    /**
     * @return OGNL expression of root object to be serialized
     */
    public String getRoot() {
        return this.root;
    }

    /**
     * Sets the root object to be serialized, defaults to the Action
     *
     * @param root OGNL expression of root object to be serialized
     */
    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * @return Generated JSON must be enclosed in comments
     */
    private boolean isWrapWithComments() {
        return this.wrapWithComments;
    }

    /**
     * @param wrapWithComments Wrap generated JSON with comments
     */
    void setWrapWithComments(boolean wrapWithComments) {
        this.wrapWithComments = wrapWithComments;
    }

    /**
     * @return Result has SMD generation enabled
     */
    public boolean isEnableSMD() {
        return this.enableSMD;
    }

    /**
     * @param enableSMD Enable SMD generation for action, which can be used for JSON-RPC
     */
    void setEnableSMD(boolean enableSMD) {
        this.enableSMD = enableSMD;
    }

    void setIgnoreHierarchy(boolean ignoreHierarchy) {
        this.ignoreHierarchy = ignoreHierarchy;
    }

    /**
     * @param ignoreInterfaces  Controls whether interfaces should be inspected for method annotations
     * You may need to set to this true if your action is a proxy as annotations
     * on methods are not inherited
     */
    public void setIgnoreInterfaces(boolean ignoreInterfaces) {
        this.ignoreInterfaces = ignoreInterfaces;
    }

    /**
     * @param enumAsBean Controls how Enum's are serialized : If true, an Enum is serialized as a
     * name=value pair (name=name()) (default) If false, an Enum is serialized
     * as a bean with a special property _name=name()
     */
    void setEnumAsBean(boolean enumAsBean) {
        this.enumAsBean = enumAsBean;
    }

    /**
     * @param noCache Add headers to response to prevent the browser from caching the response
     */
    void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    /**
     * @param excludeNullProperties Do not serialize properties with a null value
     */
    void setExcludeNullProperties(boolean excludeNullProperties) {
        this.excludeNullProperties = excludeNullProperties;
    }

    /**
     * @param statusCode Status code to be set in the response
     */
    void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    void setCallbackParameter(String callbackParameter) {
        this.callbackParameter = callbackParameter;
    }

    /**
     * @param prefix Prefix JSON with "{} &amp;&amp;"
     */
    public void setPrefix(boolean prefix) {
        this.prefix = prefix;
    }

    /**
     * @param contentType Content type to be set in the response
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @param wrapPrefix  Text to be inserted at the begining of the response
     */
    void setWrapPrefix(String wrapPrefix) {
        this.wrapPrefix = wrapPrefix;
    }

    /**
     * @param wrapSuffix  Text to be inserted at the end of the response
     */
    void setWrapSuffix(String wrapSuffix) {
        this.wrapSuffix = wrapSuffix;
    }

    /**
     * If defined will be used instead of {@link #defaultEncoding}, you can define it with result
     * &lt;result name=&quot;success&quot; type=&quot;json&quot;&gt;
     *     &lt;param name=&quot;encoding&quot;&gt;UTF-8&lt;/param&gt;
     * &lt;/result&gt;
     *
     * @param encoding valid encoding string
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Inject(required=false,value="struts.json.dateformat")
    void setDefaultDateFormat(String defaultDateFormat) {
        this.defaultDateFormat = defaultDateFormat;
    }
}
