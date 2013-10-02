import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Properties;

// import logging.LogManager;

public class LoadableProperties {
//   public LoadableProperties() { }

  protected void extractFields(Properties props)
  {
    Field[] vars= this.getClass().getFields();
    for(Field f:vars){
      int mod = f.getModifiers();
      if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
         //Try both straight name and specific name
         String propName = f.getName();
         readField(f,propName,props);
         propName = this.getClass().getSimpleName() + "." + propName;
         readField(f,propName,props);
      }
    }
    // LogManager.logDetail("Loaded properties:\n"+ toString()+"\n");
  }

  @SuppressWarnings("unchecked")
  private void readField(Field f, String propName, Properties props)
  {
    try{
      if(f.getType().isEnum()){
        Class<Enum> enumClass = (Class<Enum>)f.getType();
        try{
        f.set(this, (Enum.valueOf(enumClass,props.getProperty(propName,(f.get(this)).toString()).trim())));
        } catch(IllegalArgumentException ex){
                System.err.println(ex.getMessage());
                System.err.println("Ignoring setting "+props.getProperty(propName,(f.get(this)).toString()).trim()+", using default value of "+f.get(this)+" for option "+f.getName()+".");
        }
      }else if(f.getType().equals(EnumSet.class)){
        String propVal = props.getProperty(propName,null);
        if(propVal!=null){
        String[] propSet = propVal.split(";");
        ArrayList<Enum> enumList = new ArrayList<Enum>();
        String enumClassName = this.getClass().getName()+"$"+f.getName()+"Enum";
        try{
          Class<Enum> enumClass = (Class<Enum>)Class.forName(enumClassName);
        
          for(String propEnum:propSet){
            try{
              Enum e = Enum.valueOf(enumClass, propEnum);
              enumList.add(e);
            }catch(IllegalArgumentException ex){
              System.err.println("Enum value of "+propEnum+" not valid for class "+f.getType()+" of property "+f.getName()+"--ignoring");
            }
          }
          if(f.get(this)==null && !enumList.isEmpty()){
          f.set(this, EnumSet.copyOf(enumList));
          }else{
            ((EnumSet)f.get(this)).addAll(enumList);
          }
        }catch(ClassNotFoundException classex){
          System.err.println("Enum class not found for EnumSet property "+f.getName()+".  Add "+enumClassName+" to your LoadableProperties definition");
        }
        }
      }else if(f.getType().equals(String.class)){
              String val = props.getProperty(propName,(String)f.get(this));
              if(val!=null)
                      f.set(this,val.trim());
      }else if (f.getType().equals(boolean.class)){
        String fGetThis = ((Boolean)f.get(this)).toString();
        String getProp = props.getProperty(propName,fGetThis).trim();
        Boolean parseBool = Boolean.parseBoolean(getProp);
        if (false) { System.out.printf("What??? %s %s %s %s\n", this, fGetThis, getProp, parseBool); }
              f.set(this,parseBool);
      }else if (f.getType().equals(int.class)){
              f.set(this,Integer.parseInt(props.getProperty(propName,((Integer)f.get(this)).toString()).trim()));
      }else if (f.getType().equals(long.class)){
              f.set(this,Long.parseLong(props.getProperty(propName,((Long)f.get(this)).toString()).trim()));
      }else if (f.getType().equals(float.class)){
              f.set(this,Float.parseFloat(props.getProperty(propName,((Float)f.get(this)).toString()).trim()));
      }else if (f.getType().equals(double.class)){
              f.set(this,Double.parseDouble(props.getProperty(propName,((Double)f.get(this)).toString()).trim()));
      }
    }catch(IllegalAccessException e){
            System.err.println("Problem auto-parsing parameters:");
            e.printStackTrace();
    }
  }

  public boolean validate() { return true; }

//   @SuppressWarnings("unchecked")
//   public String toString(){
//     Field[] vars= this.getClass().getFields();
//     StringBuffer sb = new StringBuffer();
//     for(Field f:vars){
//       int mod = f.getModifiers();
//       if(Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
//         try{
//           if(f.get(this)!=null){
//             sb.append(this.getClass().getSimpleName()+"."+f.getName());
//             sb.append("=");
//               if(EnumSet.class.isAssignableFrom(f.getType().getClass())){
//                 EnumSet s = (EnumSet)f.get(this);
//                 for(Object val:s){
//                   sb.append(((Enum)val).name()+";");
//                 }
//               }else{
//                   sb.append(f.get(this).toString());
//               }
//             sb.append("\n");
//           }
//         }catch(IllegalAccessException e){
//                 System.err.println("Problem printing parameters--this shouldn't happen:");
//                 e.printStackTrace();
//         }
//       }
//     }
//     return sb.toString();
//   }
//   
//   @SuppressWarnings("unchecked")
//   public String toPerlHashPairs() {
//     Field[] vars= this.getClass().getFields();
//     StringBuffer sb = new StringBuffer();
//     for(Field f:vars){
//       int mod = f.getModifiers();
//       if(Modifier.isPublic(mod) && !Modifier.isStatic(mod)){
//         try{
//           if(f.get(this)!=null){
//             sb.append("\""+this.getClass().getSimpleName()+"."+f.getName());
//             sb.append("\"=>\"");
//             if(EnumSet.class.isAssignableFrom(f.getType().getClass())){
//               EnumSet s = (EnumSet)f.get(this);
//               for(Object val:s){
//                 sb.append(((Enum)val).name()+";");
//               }
//             }else{
//                 sb.append(f.get(this).toString());
//             }
//             sb.append("\",\n");
//           }
//         }catch(IllegalAccessException e){
//                 System.err.println("Problem printing parameters--this shouldn't happen:");
//                 e.printStackTrace();
//         }
//       }
//     }
//     sb.delete(sb.length()-2, sb.length());
//     return sb.toString();
//   }
// 
}
