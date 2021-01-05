/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.device;

import java.util.ArrayList;
import wrapper.core.CPlatform;

/**
 *
 * @author user
 */
public class DeviceInformation1 {
    public static void main(String... args)
    {        
        DeviceQuery query = new DeviceQuery(new ArrayList<>(CPlatform.getPlatforms()));
        query.getInformation();
    }
}
