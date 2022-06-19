/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.util;

/**
 *
 * @author user
 */
public final class CLOptions 
{
    private final String options;
    
    private CLOptions(String options)
    {
        this.options = options;
    }
    
    public static CLOptions include(String... directories)
    {
        if(directories == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (String directorie : directories) {
            builder.append(" -I ").append(directorie);
        }
        return new CLOptions(builder.toString());
    }
    
    public String getOptions()
    {
        return options;
    }
}
