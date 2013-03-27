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

import com.google.gwt.user.client.Event;

/**
 * A class to hold event values for the Slider
 */
public class SliderEvent
{
    private int[] m_values;
    private boolean m_hasOriginalEvent = true;
    private Slider m_source;
    private Event m_event;
    
    /**
     * Create a new slider event.
     * 
     * @param event - the event received by JSNI called
     * @param source - the Slider that fires the event
     * @param values - int array of values
     */
    public SliderEvent(Event event, Slider source, int[] values)
    {
        this(event, source, values, true);
    }

    /**
     * Create a new slider event.
     * 
     * @param event - the event received by JSNI called
     * @param source - the Slider that fires the event
     * @param values - int array of values
     * @param hasOriginalEvent - boolean if the change came from a non-programmatic change such as mouse or keyboard event
     */
    public SliderEvent(Event event, Slider source, int[] values, boolean hasOriginalEvent)
    {
        m_source = source;
        m_event = event;
        m_values = values;
        m_hasOriginalEvent = hasOriginalEvent;
    }
    
    /**
     * @return Returns the JSNI returned JavaScriptObject event.
     */
    public Event getEvent()
    {
        return m_event;
    }

    /**
     * Get the source of the event.
     * 
     * @return Returns the source.
     */
    public Slider getSource()
    {
        return m_source;
    }
    
    /**
     * Get the values from the event.
     * 
     * @return Returns the value.
     */
    public int[] getValues()
    {
        return m_values;
    }

    /**
     * Does this event have an original event.
     * 
     * @return Returns the hasOriginalEvent.
     */
    public boolean hasOriginalEvent()
    {
        return m_hasOriginalEvent;
    }

}
