<?xml version="1.0" encoding="UTF-8"?>
<WebServiceRequestEntity>
   <description></description>
   <name>API Signing Doc Copy 2</name>
   <tag></tag>
   <elementGuidId>e868ee28-c2a5-4b31-8c06-01da8ab6d167</elementGuidId>
   <selectorMethod>BASIC</selectorMethod>
   <useRalativeImagePath>false</useRalativeImagePath>
   <connectionTimeout>-1</connectionTimeout>
   <followRedirects>false</followRedirects>
   <httpBody></httpBody>
   <httpBodyContent>{
  &quot;text&quot;: &quot;{\n \&quot;documents\&quot;: [\n  {\n   \&quot;docToken\&quot;: \&quot;f533d44d701a908997d5b4f7fff0cf08bb1cf110d2c4db420dd695fb14a1ce39\&quot;,\n        \&quot;x\&quot;:\&quot;63\&quot;,\n        \&quot;y\&quot;:\&quot;320\&quot;,\n        \&quot;page\&quot;:\&quot;4\&quot;\n  }\n  ],\n \&quot;signature\&quot;: {\n  \&quot;visibility\&quot; : true\n }\n}&quot;,
  &quot;contentType&quot;: &quot;text/plain&quot;,
  &quot;charset&quot;: &quot;UTF-8&quot;
}</httpBodyContent>
   <httpBodyType>text</httpBodyType>
   <httpHeaderProperties>
      <isSelected>true</isSelected>
      <matchCondition>equals</matchCondition>
      <name>Authorization</name>
      <type>Main</type>
      <value>Basic e3t1c2VybmFtZX19Ont7cGFzc3dvcmR9fQ==</value>
      <webElementGuid>ae6680dd-79a4-43f4-b27d-4e1b662f6f4b</webElementGuid>
   </httpHeaderProperties>
   <httpHeaderProperties>
      <isSelected>false</isSelected>
      <matchCondition>equals</matchCondition>
      <name>Merchant-Key</name>
      <type>Main</type>
      <value>${merchant-key}</value>
      <webElementGuid>4e024c00-2a57-48f7-a2cc-ee211e738797</webElementGuid>
   </httpHeaderProperties>
   <httpHeaderProperties>
      <isSelected>false</isSelected>
      <matchCondition>equals</matchCondition>
      <name>Token</name>
      <type>Main</type>
      <value>${access_token}</value>
      <webElementGuid>428713e7-faa3-4e92-91f4-6e12dfaebe7a</webElementGuid>
   </httpHeaderProperties>
   <katalonVersion>8.5.5</katalonVersion>
   <maxResponseSize>-1</maxResponseSize>
   <migratedVersion>5.4.1</migratedVersion>
   <restRequestMethod>POST</restRequestMethod>
   <restUrl>https://stg-core.privy.id/v2/merchant/document/multiple-signing</restUrl>
   <serviceType>RESTful</serviceType>
   <soapBody></soapBody>
   <soapHeader></soapHeader>
   <soapRequestMethod></soapRequestMethod>
   <soapServiceEndpoint></soapServiceEndpoint>
   <soapServiceFunction></soapServiceFunction>
   <socketTimeout>-1</socketTimeout>
   <useServiceInfoFromWsdl>true</useServiceInfoFromWsdl>
   <variables>
      <defaultValue>GlobalVariable.merchant-key</defaultValue>
      <description></description>
      <id>8f6e4dd0-87e9-4559-a8f5-a30f045e0583</id>
      <masked>false</masked>
      <name>merchant-key</name>
   </variables>
   <variables>
      <defaultValue>GlobalVariable.access_token</defaultValue>
      <description></description>
      <id>445c46ff-02da-42ca-9bbc-9edd6950ed52</id>
      <masked>false</masked>
      <name>access_token</name>
   </variables>
   <wsdlAddress></wsdlAddress>
</WebServiceRequestEntity>
