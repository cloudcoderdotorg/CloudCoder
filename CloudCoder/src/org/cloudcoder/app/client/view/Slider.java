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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;


/**
 * This widget wraps the JQuery UI Slider
 * and allows for single slider or double slider with range.
 * 
 * All options can be get or set using generic get/setIntOption, get/setStringOption, get/setBooleanOption
 * methods, but some convenience methods are provided for most popular such as
 * setValues and setMinimum and setMaximum.  See SliderOptions for full list of options.
 * @see SliderOption
 */
public class Slider extends Widget
{    
    private JSONObject m_defaultOptions;
    private List<SliderListener> m_listeners = new ArrayList<SliderListener>();
    
    /**
     * Create a slider with the specified ID.  The ID is required
     * because the slider needs a specific ID to connect to.
     * @param id - id of the element
     * @param min - default minimum of the slider
     * @param max - default maximum of the slider
     * @param defaultValue - default point of a single anchor
     */
    public Slider(String id, int min, int max, int defaultValue)
    {
        this(id, min, max, new int[]{defaultValue});
    }
        
    /**
     * Create a slider with the specified ID.  The ID is required
     * because the slider needs a specific ID to connect to.
     * @param id - id of the element
     * @param min - default minimum of the slider
     * @param max - default maximum of the slider
     * @param defaultValues - default points of each anchor
     */
    public Slider(String id, int min, int max, int[] defaultValues)
    {           
        this(id, getOptions(min, max, defaultValues));
    }
    
    /**
     * Create the default slider with the specified ID.  The ID is required
     * because the slider needs a specific ID to connect to.
     * @param id - id of the element to create
     */
    public Slider(String id)
    {           
        this(id, null);        
    }
    
    /**
     * Create a slider with the specified ID.  The ID is required
     * because the slider needs a specific ID to connect to.
     * @param id - id of the element to create
     * @param options - JSONObject of any possible option, can be null for defaults
     */
    public Slider(String id, JSONObject options)
    {           
        super();
        Element divEle = DOM.createDiv();
        setElement(divEle);
        divEle.setId(id);
        
        m_defaultOptions = options;
        if (m_defaultOptions == null) {
            m_defaultOptions = getOptions(0, 100, new int[]{0});
        }        
    }
    
    /**
     * A convenient way to create an options JSONObject.  Use SliderOption for keys.
     * @param min - default minimum of the slider
     * @param max - default maximum of the slider
     * @param defaultValues - default points of each anchor
     * @return a JSONObject of Slider options
     */
    public static JSONObject getOptions(int min, int max, int[] defaultValues) 
    {
        JSONObject options = new JSONObject();
        options.put(SliderOption.MIN.toString(), new JSONNumber(min));
        options.put(SliderOption.MAX.toString(), new JSONNumber(max));
        JSONArray vals = intArrayToJSONArray(defaultValues);
        options.put(SliderOption.VALUES.toString(), vals);
        return options;
    }

    private static JSONArray intArrayToJSONArray(int[] values)
    {
        JSONArray vals = new JSONArray(); 
        for (int i = 0, len = values.length; i < len; i++) {
            vals.set(i, new JSONNumber(values[i]));
        }
        return vals;
    }
    
    @Override
    protected void onLoad()
    {
        createSliderJS(this, getElement().getId(), m_defaultOptions.getJavaScriptObject());
        super.onLoad();
    }
    
    @Override
    protected void onUnload()
    {
        destroySliderJS(this, getElement().getId());
        super.onUnload();        
    }
    /**
     * Gets the minimum possible value for the slider
     * @return Returns the minimum.
     */
    public int getMinimum()
    {
        return getIntOptionJS(getElement().getId(), SliderOption.MIN.toString());
    }

    /**
     * Sets the minimum possible value for the slider
     * @param minimum The minimum to set.
     */
    public void setMinimum(int minimum)
    {
        setIntOptionJS(getElement().getId(), SliderOption.MIN.toString(), minimum);
    }

    /**
     * Gets the maximum possible value for the slider
     * @return Returns the maximum.
     */
    public int getMaximum()
    {
        return getIntOptionJS(getElement().getId(), SliderOption.MAX.toString());
    }

    /**
     * Sets the maximum possible value for the slider
     * @param maximum The maximum to set.
     */
    public void setMaximum(int maximum)
    {
        setIntOptionJS(getElement().getId(), SliderOption.MAX.toString(), maximum);
    }

    /**
     * Convenience method for only 1 anchor
     * @return Returns the value.
     */
    public int getValue()
    {
        return getValueAtIndex(0);
    }
    
    /**
     * Convenience method for only 1 anchor
     * @param value to set.
     */
    public void setValue(int value)
    {
        int[] values = {value};
        setValues(values);
    }    
    
    /**
     * Sets the value of each anchor
     * @param values - int array of values
     */
    public void setValues(int[] values)
    {
        JSONArray vals = intArrayToJSONArray(values);
        setValuesJS(getElement().getId(), vals.getJavaScriptObject());
    }
        
    /**
     * Gets the value of a anchor at the specified index
     * 
     * @param index  the index to retreive the value for
     * 
     * @return the value
     */
    public int getValueAtIndex(int index)
    {
        return getValueJS(getElement().getId(), index);
    }
    
    /**
     * Set an option numeric value
     * @param option the SliderOption
     * @param value the numeric
     */
    public void setIntOption(SliderOption option, int value) 
    {
        setIntOptionJS(getElement().getId(), option.toString(), value);
    }
    
    /**
     * Get an option numeric value
     * @param option the SliderOption
     * @return value the numeric
     */
    public int getIntOption(SliderOption option) 
    {
        return getIntOptionJS(getElement().getId(), option.toString());
    }
    
    /**
     * Set an option boolean value
     * @param option the SliderOption
     * @param value the boolean
     */
    public void setBooleanOption(SliderOption option, boolean value) 
    {
        setBooleanOptionJS(getElement().getId(), option.toString(), value);
    }
    
    /**
     * Get an option boolean value
     * @param option the SliderOption
     * @return value the boolean
     */
    public boolean getBooleanOption(SliderOption option) 
    {
        return getBooleanOptionJS(getElement().getId(), option.toString());
    }
    
    /**
     * Set an option string value
     * @param option the SliderOption
     * @param value the String
     */
    public void setStringOption(SliderOption option, String value) 
    {
        setStringOptionJS(getElement().getId(), option.toString(), value);
    }
    
    /**
     * Set an option string value
     * @param option the SliderOption
     * @return value the String
     */
    public String setStringOption(SliderOption option) 
    {
        return getStringOptionJS(getElement().getId(), option.toString());
    }
    
    /**
     * Add a SliderListener
     * @param l - SliderListener
     */
    public void addListener(SliderListener l) 
    {
        m_listeners.add(l);
    }
    
    /**
     * Removes the SliderListener
     * @param l - SliderListener
     */
    public void removeListener(SliderListener l)
    {
        m_listeners.remove(l);
    }
    

    private void fireOnStartEvent(Event evt, JsArrayInteger values)
    {
        int[] vals = jsArrayIntegerToIntArray(values);
        SliderEvent e = new SliderEvent(evt, this, vals);
        
        for (SliderListener l : m_listeners) {
            l.onStart(e);
        }
    }
    
    private boolean fireOnSlideEvent(Event evt, JsArrayInteger values)
    {
        int[] vals = jsArrayIntegerToIntArray(values);
        SliderEvent e = new SliderEvent(evt, this, vals);
        
        for (SliderListener l : m_listeners) {
            l.onStart(e);
        }
        
        boolean ret = true;
        
        for (SliderListener l : m_listeners) {
            if (!l.onSlide(e)) {
                //if any of the listeners returns false, return false,
                //but let them all do their thing
                ret = false;
            }
        }
        
        return ret;
    }
    
    private void fireOnChangeEvent(Event evt, JsArrayInteger values, boolean hasOriginalEvent)
    {
        int[] vals = jsArrayIntegerToIntArray(values);        
        SliderEvent e = new SliderEvent(evt, this, vals, hasOriginalEvent);
        
        for (SliderListener l : m_listeners) {
            l.onChange(e);
        }
    }
    
    private void fireOnStopEvent(Event evt, JsArrayInteger values)
    {
        int[] vals = jsArrayIntegerToIntArray(values);
        SliderEvent e = new SliderEvent(evt, this, vals);
        
        for (SliderListener l : m_listeners) {
            l.onStop(e);
        }
    }
    
    private int[] jsArrayIntegerToIntArray(JsArrayInteger values)
    {
        int len = values.length();
        int[] vals = new int[len];
        for (int i = 0; i < len; i++) {
            vals[i] = values.get(i);
        }
        return vals;
    }
    
    /*
     * JSNI methods 
     */
    
    private native void setIntOptionJS(String id, String option, int value) /*-{
        $wnd.$("#" + id).slider("option", option, value);
    }-*/;
    
    
    private native int getIntOptionJS(String id, String option) /*-{
        return $wnd.$("#" + id).slider("option", option);
    }-*/;

    
    private native void setBooleanOptionJS(String id, String option, boolean value) /*-{
        $wnd.$("#" + id).slider("option", option, value);
    }-*/;
    
    
    private native boolean getBooleanOptionJS(String id, String option) /*-{
        return $wnd.$("#" + id).slider("option", option);
    }-*/;

    
    private native void setStringOptionJS(String id, String option, String value) /*-{
        $wnd.$("#" + id).slider("option", option, value);
    }-*/;
    
    
    private native String getStringOptionJS(String id, String option) /*-{
        return $wnd.$("#" + id).slider("option", option);
    }-*/;

    
    private native void setValuesJS(String id, JavaScriptObject values) /*-{
        $wnd.$("#" + id).slider("option", "values", values);
    }-*/;
    
    
    private native int getValueJS(String id, int index) /*-{
        return $wnd.$("#" + id).slider("values", index);
    }-*/;
    
    
    private native void createSliderJS(Slider x, String id, JavaScriptObject options) /*-{
        options.start = function(event, ui) {
            x.@org.cloudcoder.app.client.view.Slider::fireOnStartEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/core/client/JsArrayInteger;)(event, ui.values);
        };
        options.slide = function(event, ui) {
            return x.@org.cloudcoder.app.client.view.Slider::fireOnSlideEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/core/client/JsArrayInteger;)(event, ui.values);
        };
        options.change = function(event, ui) {
            var has = event.originalEvent ? true : false;
            x.@org.cloudcoder.app.client.view.Slider::fireOnChangeEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/core/client/JsArrayInteger;Z)(event, ui.values, has);                
        };
        options.stop = function(event, ui) {
            x.@org.cloudcoder.app.client.view.Slider::fireOnStopEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/core/client/JsArrayInteger;)(event, ui.values);
        };
        
        $wnd.$("#" + id).slider(options);
    }-*/;


    private native void destroySliderJS(Slider x, String id) /*-{
        $wnd.$("#" + id).slider("destroy");
    }-*/;
}
