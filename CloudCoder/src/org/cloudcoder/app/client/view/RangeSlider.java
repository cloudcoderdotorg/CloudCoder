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

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;



/**
 * This is a convenient sub class of Slider just for ranges
 */
public class RangeSlider extends Slider
{    
    /**
     * Constructor for the RangeSlider
     * @param id - element ID
     * @param min - the minimum possible value of the slider
     * @param max - the maximum possible value of the slider
     * @param defaultMin - the default value of the lowest anchor
     * @param defaultMax - the default value of the highest anchor
     */
    public RangeSlider(String id, int min, int max, int defaultMin, int defaultMax)
    {
        super(id, getOptions(min, max, defaultMin, defaultMax));
    }

    /**
     * A convenient way to create an options JSONObject for the RangeSlider.
     * @param min - default minimum of the slider
     * @param max - default maximum of the slider
     * @param defaultMin - the default value of the lowest anchor
     * @param defaultMax - the default value of the highest anchor
     * @return a JSONObject of RangeSlider options
     */
    public static JSONObject getOptions(int min, int max, int defaultMin, int defaultMax) 
    {
        JSONObject options = Slider.getOptions(min, max, new int[]{defaultMin, defaultMax});
        options.put(SliderOption.RANGE.toString(), JSONBoolean.getInstance(true));        
        return options;
    }
    
    /**
     * Convenience method for when range is true, gets the minimum of the selected range, or in other words,
     * gets the value of the lower anchor
     * @return the value
     */
    public int getValueMin()
    {
        return getValueAtIndex(0);
    }
    
    /**
     * Convenience method for when range is true, gets the maximum of the selected range, or in other words,
     * gets the value of the higher anchor
     * @return the value
     */
    public int getValueMax()
    {
        return getValueAtIndex(1);
    }

    /**
     * Convenience method for when range is true, sets both the min and max anchors
     * @param min - the lower anchor's value
     * @param max - the upper anchor's value
     */
    public void setValues(int min, int max)
    {
        setValues(new int[]{min, max});
    }
    
}
