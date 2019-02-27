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
package org.apache.struts2.jasper;

import org.apache.struts2.jasper.compiler.ErrorDispatcher;
import org.apache.struts2.jasper.compiler.JavacErrorDetail;
import org.apache.struts2.jasper.compiler.Localizer;
import org.apache.struts2.jasper.servlet.JspServletWrapper;

import javax.servlet.ServletException;

/**
 * Base class for all exceptions generated by the JSP engine. Makes it
 * convenient to catch just this at the top-level.
 *
 * @author Anil K. Vijendran
 */
public class JasperException extends javax.servlet.ServletException {
    
    public JasperException(String reason) {
	super(reason);
    }

    /**
     * Creates a JasperException with the embedded exception and the reason for
     * throwing a JasperException
     *
     * @param reason the reason
     * @param exception the exception
     */
    public JasperException (String reason, Throwable exception) {
   	super(reason, exception);
    }

    /**
     * Creates a JasperException with the embedded exception
     *
     * @param exception the exception
     */
    public JasperException (Throwable exception) {
   	super(exception);
    }

    /**
     * <p>Attempts to construct a JasperException that contains helpful information
     * about what went wrong. Uses the JSP compiler system to translate the line
     * number in the generated servlet that originated the exception to a line
     * number in the JSP.  Then constructs an exception containing that
     * information, and a snippet of the JSP to help debugging.
     * Please see http://issues.apache.org/bugzilla/show_bug.cgi?id=37062 and
     * http://www.tfenne.com/jasper/ for more details.
     *</p>
     *
     * @param ex the exception that was the cause of the problem.
     * @param jspServletWrapper
     * @return a JasperException with more detailed information
     */
    public JasperException handleJspException(Exception ex, JspServletWrapper jspServletWrapper, JspCompilationContext ctxt, Options options) {
        try {
            Throwable realException = ex;
            if (ex instanceof ServletException) {
                realException = ((ServletException) ex).getRootCause();
            }

            // First identify the stack frame in the trace that represents the JSP
            StackTraceElement[] frames = realException.getStackTrace();
            StackTraceElement jspFrame = null;

            for (int i=0; i<frames.length; ++i) {
                if ( frames[i].getClassName().equals(jspServletWrapper.getServlet().getClass().getName()) ) {
                    jspFrame = frames[i];
                    break;
                }
            }

            if (jspFrame == null) {
                // If we couldn't find a frame in the stack trace corresponding
                // to the generated servlet class, we can't really add anything
                return new JasperException(ex);
            }
            else {
                int javaLineNumber = jspFrame.getLineNumber();
                JavacErrorDetail detail = ErrorDispatcher.createJavacError(
                        jspFrame.getMethodName(),
                        ctxt.getCompiler().getPageNodes(),
                        null,
                        javaLineNumber,
                        ctxt);

                // If the line number is less than one we couldn't find out
                // where in the JSP things went wrong
                int jspLineNumber = detail.getJspBeginLineNumber();
                if (jspLineNumber < 1) {
                    throw new JasperException(ex);
                }

                if (options.getDisplaySourceFragment()) {
                    return new JasperException(Localizer.getMessage
                            ("jsp.exception", detail.getJspFileName(),
                                    "" + jspLineNumber) +
                                    "\n\n" + detail.getJspExtract() +
                                    "\n\nStacktrace:", ex);

                } else {
                    return new JasperException(Localizer.getMessage
                            ("jsp.exception", detail.getJspFileName(),
                                    "" + jspLineNumber), ex);
                }
            }
        } catch (Exception je) {
            // If anything goes wrong, just revert to the original behaviour
            if (ex instanceof JasperException) {
                return (JasperException) ex;
            } else {
                return new JasperException(ex);
            }
        }
    }
}
