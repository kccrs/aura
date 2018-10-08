/*
 * Copyright (C) 2013 salesforce.com, inc.
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
package org.auraframework.util.javascript.builder;

public class JavascriptResource {
    private String output;
    private String input;
    private String sourcemap;

    public JavascriptResource(String input, String output, String sourcemap) {
        this.input = input;
        this.output = output;
        this.sourcemap = sourcemap;
    }


    public String getOutput() {
        return output;
    }

    public String getInput() {
        return input;
    }

    public String getSourcemap() {
        return sourcemap;
    }
}