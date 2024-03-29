/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onpositive.semantic.wikipedia.abstracts.svc;

import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.parser.nodes.WtUrl;

public interface HtmlRendererCallback
{
	public MediaInfo getMediaInfo(String title, int width, int height) throws Exception;
	
	public boolean resourceExists(PageTitle target);
	
	public String makeUrl(PageTitle linkTarget);
	
	public String makeUrl(WtUrl target);
	
	public String makeUrlMissingTarget(String path);
}
