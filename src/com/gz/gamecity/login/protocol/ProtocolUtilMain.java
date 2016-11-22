package com.gz.gamecity.login.protocol;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


public class ProtocolUtilMain {

	public static void main(String[] args) {
		
		String xmlpath = "./conf/protocol.xml";
		SAXBuilder builder = new SAXBuilder(false);
		List<ProtocolBean> list=new ArrayList<ProtocolBean>();
//		List<Element> 
		try {
			Document doc = builder.build(xmlpath);
			Element servers = doc.getRootElement();
			List serverlist = servers.getChildren("protocol");
			List<Element> protocol_list=new ArrayList<>();
			for (Iterator iter = serverlist.iterator(); iter.hasNext();) {
				Element protocol = (Element) iter.next();
				protocol_list.add(protocol);
				List fieldList=protocol.getChildren("field");
				System.out.println(fieldList);
				for(int i=0;i<fieldList.size();i++){
					Element e = (Element) fieldList.get(i);
					String fieldName=e.getAttributeValue("name");
					System.out.println(fieldName);
				}
				String className=protocol.getAttributeValue("name");
				ProtocolBean p=new ProtocolBean();
				p.name = className;
				p.fieldList=fieldList;
				list.add(p);
			}
			createJavaFile2(protocol_list);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static void createJavaFile2(List<Element> fieldList){
		FileWriter writer=null;
		try {
			writer = new FileWriter("./src/com/gz/gamecity/login/protocol/ProtocolsField.java");
			writer.write("package com.gz.gamecity.login.protocol;"+"\r\n");
			writer.write("\r\n");
			writer.write("public class ProtocolsField{"+"\r\n");
			writer.write("\r\n");
			for(Element e:fieldList){
				
				writeClass2(e,writer,tab1);
				
			}

			writer.write("\r\n");
			writer.write(tab1+"public static final String MAINCODE = \"mainCode\";"+"\r\n");
			writer.write(tab1+"public static final String SUBCODE = \"subCode\";"+"\r\n");
			writer.write(tab1+"public static final String ERRORCODE = \"errorCode\";"+"\r\n");
			
			writer.write("\r\n");
			writer.write("}");
			writer.flush();
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	public static void writeClass2(Element element,FileWriter writer,String tab){
		String className = element.getAttributeValue("name");
		className=className.substring(0, 1).toUpperCase()+className.substring(1, className.length());
		String mainCode = element.getChildText("mainCode");
		String subCode = element.getChildText("subCode");
		
		try {
			writer.write("\r\n");
			writer.write(tab +"public static final class "+className+"{"+"\r\n" );
			
			if(mainCode!=null && !mainCode.equals("")){
				writer.write(tab + tab1 +"public static final int mainCode_value = "+ mainCode+";\r\n");
			}
			if(subCode!=null && !subCode.equals("")){
				writer.write(tab + tab1 +"public static final int subCode_value = "+ subCode+";\r\n");
			}
			List fieldList = element.getChildren("field");
			for(int i=0;i<fieldList.size();i++){
				Element e = (Element) fieldList.get(i);
				String field = e.getAttributeValue("name");
				writer.write(tab + tab1 +"public static final String " + field.toUpperCase()+" = \""+ field+"\";\r\n");
				if(e.getAttributeValue("type")!=null && e.getAttributeValue("type").equals("obj")){
					String t=tab+tab1;
					writeClass2(e,writer,t);
				}
				
			}
			
			
			writer.write("\r\n");
			writer.write(tab+"}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

	static String tab1="	";
	static String tab2=tab1+"	";
	static String tab3=tab2+"	";
	static String tab4=tab3+"	";
	
	public static void createJavaFile(List<ProtocolBean> list){
		FileWriter writer=null;
		try {
			writer = new FileWriter("./src/com/gz/gamecity/login/protocol/ProtocolsField.java");
			writer.write("package com.gz.gamecity.login.protocol;"+"\r\n");
			writer.write("\r\n");
			writer.write("public class ProtocolsField{"+"\r\n");
			writer.write("\r\n");
			for(ProtocolBean protocol:list){
				
				writeClass(protocol.name,protocol.fieldList,tab1,writer);
				
			}

			writer.write("\r\n");
			writer.write(tab1+"public static final String MAINCODE = \"errorCode\";"+"\r\n");
			writer.write(tab1+"public static final String SUBCODE = \"errorCode\";"+"\r\n");
			writer.write(tab1+"public static final String ERRORCODE = \"errorCode\";"+"\r\n");
			
			writer.write("\r\n");
			writer.write("}");
			writer.flush();
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void writeClass(String _className,List fieldList,String tab,FileWriter writer){
		String className=_className.substring(0, 1).toUpperCase()+_className.substring(1, _className.length());
		try {
			writer.write("\r\n");
			writer.write(tab +"public static final class "+className+"{"+"\r\n" );
			for(int i=0;i<fieldList.size();i++){
				Element e = (Element) fieldList.get(i);
				if(e.getAttributeValue("type")==null){
					String field = e.getAttributeValue("name");
					writer.write(tab + tab1 +"public static final String " + field.toUpperCase()+" = \""+ field+"\";\r\n");
					if(e.getText()!=null && !e.getText().equals("")){
						writer.write(tab + tab1 +"public static final int " + field+"_value = "+ e.getTextTrim()+";\r\n");
					}
					
				}else{
					String field = e.getAttributeValue("name");
					writer.write(tab + tab1 +"public static final String " + field.toUpperCase()+" = \""+ field+"\";\r\n");
					
					
					String name = e.getAttributeValue("name");
					List list = e.getChildren("field");
					String t=tab+tab1;
					writeClass(name,list,t,writer);
				}
				
			}
			
			
			writer.write("\r\n");
			writer.write(tab+"}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
