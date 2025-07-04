/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package ch.randelshofer.quaqua.ext.batik.ext.awt;

import java.awt.RenderingHints;

/**
 * TranscodingHint as to what the destination of the drawing is.
 *
 * @author <a href="mailto:deweese@apache.org">Thomas DeWeese</a>
 * @version $Id: TranscodingHintKey.java 475477 2006-11-15 22:44:28Z cam $
 */
final class TranscodingHintKey extends RenderingHints.Key {

	TranscodingHintKey(int number) {
		super(number);
	}

	@Override
	public boolean isCompatibleValue(Object val) {
		boolean isCompatible = true;
		if ((val != null) && !(val instanceof String)) {
			isCompatible = false;
		}
		return isCompatible;
	}
}
