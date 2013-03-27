/*******************************************************************************
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
 *
 ******************************************************************************/
package org.cloudcoder.app.client.view;

/**
 * An enumeration of all the options you can set for a slider
 */
public enum SliderOption
{
    /**
     * disabled. Type Boolean. Default: false
     * Disables (true) or enables (false) the slider. Can be set when initialising (first creating) the slider.
     * 
     */
    DISABLED("disabled"),

    /**
     * animate. Type: Boolean, String, Number. Default: false
     * Whether to slide handle smoothly when user click outside handle on the bar. 
     * Will also accept a string representing one of the three predefined speeds ("slow", "normal", or "fast") 
     * or the number of milliseconds to run the animation (e.g. 1000).
     */
    ANIMATE("animate"),

    /**
     * max. Type: Number. Default: 100
     * The maximum value of the slider.
     */
    MAX("max"),

    /**
     * min. Type: Number. Default: 0
     * The minimum value of the slider.
     */
    MIN("min"),

    /**
     * orientation. Type: String. Default: 'horizontal'
     * This option determines whether the slider has the min at the left,
     * the max at the right or the min at the bottom, the max at the top. 
     * Possible values: 'horizontal', 'vertical'..
     */
    ORIENTATION("orientation"),

    /**
     * range. Type: Boolean, String. Default: false
     * If set to true, the slider will detect if you have two handles and create a stylable range element between these two. 
     * Two other possible values are 'min' and 'max'. 
     * A min range goes from the slider min to one handle. 
     * A max range goes from one handle to the slider max.
     */
    RANGE("range"),

    /**
     * step. Type: Number. Default: 1
     * Determines the size or amount of each interval or step the slider takes between min and max. 
     * The full specified value range of the slider (max - min) needs to be evenly divisible by the step.
     */
    STEP("step"),
    
    /**
     * value. Type: Number. Default: 0
     * Determines the value of the slider, if there's only one handle. 
     * If there is more than one handle, determines the value of the first handle.
     */
    VALUE("value"),

    /**
     * values. Type: Array. Default: null
     * This option can be used to specify multiple handles. If range is set to true, the length of 'values' should be 2.
     */
    VALUES("values");

    private String m_name;

    private SliderOption(String name)
    {
        m_name = name;
    }

    @Override
    public String toString()
    {
        return m_name;
    }
}