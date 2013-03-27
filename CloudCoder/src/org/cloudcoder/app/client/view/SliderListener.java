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
 * The listener interface for the sliders
 *
 */
public interface SliderListener
{
    /**
     * This event is triggered when the user starts sliding
     * @param e SliderEvent
     */
    public void onStart(SliderEvent e);
    
    /**
     * This event is triggered on every mouse move during slide. 
     * Return false in order to prevent a slide, based on a value.
     * @param e SliderEvent
     * @return boolean false to prevent the slide
     */
    public boolean onSlide(SliderEvent e);
    
    /**
     * This event is triggered on slide stop, or if the value is changed programmatically (by the value method). 
     * Use SliderEvent.hasOriginalEvent() to detect whether the value changed by mouse or keyboard.  When false
     * it means the change was done programmatically. 
     * @param e SliderEvent
     */
    public void onChange(SliderEvent e);
    
    /**
     * This event is triggered when the user stops sliding.
     * @param e SliderEvent
     */
    public void onStop(SliderEvent e);
}
