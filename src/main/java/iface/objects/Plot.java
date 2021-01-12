

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package iface.objects;

import com.rti.dds.infrastructure.*;
import com.rti.dds.infrastructure.Copyable;
import java.io.Serializable;
import com.rti.dds.cdr.CdrHelper;

public class Plot   implements Copyable, Serializable{

    public double azimuth= 0;
    public double range= 0;
    public double elevation= 0;
    public double doppler= 0;

    public Plot() {

    }
    public Plot (Plot other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        Plot self;
        self = new  Plot();
        self.clear();
        return self;

    }

    public void clear() {

        azimuth= 0;
        range= 0;
        elevation= 0;
        doppler= 0;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        Plot otherObj = (Plot)o;

        if(azimuth != otherObj.azimuth) {
            return false;
        }
        if(range != otherObj.range) {
            return false;
        }
        if(elevation != otherObj.elevation) {
            return false;
        }
        if(doppler != otherObj.doppler) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        __result += (int)azimuth;
        __result += (int)range;
        __result += (int)elevation;
        __result += (int)doppler;
        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>PlotTypeSupport</code>
    * rather than here by using the <code>-noCopyable</code> option
    * to rtiddsgen.
    * 
    * @param src The Object which contains the data to be copied.
    * @return Returns <code>this</code>.
    * @exception NullPointerException If <code>src</code> is null.
    * @exception ClassCastException If <code>src</code> is not the 
    * same type as <code>this</code>.
    * @see com.rti.dds.infrastructure.Copyable#copy_from(java.lang.Object)
    */
    public Object copy_from(Object src) {

        Plot typedSrc = (Plot) src;
        Plot typedDst = this;

        typedDst.azimuth = typedSrc.azimuth;
        typedDst.range = typedSrc.range;
        typedDst.elevation = typedSrc.elevation;
        typedDst.doppler = typedSrc.doppler;

        return this;
    }

    public String toString(){
        return toString("", 0);
    }

    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();        

        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }

        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("azimuth: ").append(azimuth).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("range: ").append(range).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("elevation: ").append(elevation).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("doppler: ").append(doppler).append("\n");  

        return strBuffer.toString();
    }

}
