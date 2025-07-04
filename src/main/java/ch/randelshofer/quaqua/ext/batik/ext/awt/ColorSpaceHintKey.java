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
 * @version $Id: ColorSpaceHintKey.java 475477 2006-11-15 22:44:28Z cam $
 */
public final class ColorSpaceHintKey extends RenderingHints.Key {

	/**
	 * Notice to source that we prefer an Alpha RGB Image.
	 */
	public final static Object VALUE_COLORSPACE_ARGB = new Object();

	/**
	 * Notice to source that we will not use Alpha Channel but we still want RGB
	 * data.
	 */
	public final static Object VALUE_COLORSPACE_RGB = new Object();

	/**
	 * Notice to source that we only want Greyscale data (no Alpha).
	 */
	public final static Object VALUE_COLORSPACE_GREY = new Object();

	/**
	 * Notice to source that we only want Greyscale data with an alpha channel.
	 */
	public final static Object VALUE_COLORSPACE_AGREY = new Object();

	/**
	 * Notice to source that we only want an alpha channel. The source should simply
	 * render alpha (no conversion)
	 */
	public final static Object VALUE_COLORSPACE_ALPHA = new Object();

	/**
	 * Notice to source that we only want an alpha channel. The source should follow
	 * the SVG spec for how to convert ARGB, RGB, Grey and AGrey to just an Alpha
	 * channel.
	 */
	public final static Object VALUE_COLORSPACE_ALPHA_CONVERT = new Object();

	public static final String PROPERTY_COLORSPACE = "ch.randelshofer.quaqua.ext.batik.gvt.filter.Colorspace";

	/**
	 * Note that this is package private.
	 */
	ColorSpaceHintKey(int number) {
		super(number);
	}

	@Override
	public boolean isCompatibleValue(Object val) {
		if ((val == VALUE_COLORSPACE_ARGB) || (val == VALUE_COLORSPACE_RGB) || (val == VALUE_COLORSPACE_GREY)
				|| (val == VALUE_COLORSPACE_AGREY)) {
			return true;
		}
		if ((val == VALUE_COLORSPACE_ALPHA) || (val == VALUE_COLORSPACE_ALPHA_CONVERT)) {
			return true;
		}
		return false;
	}
}
