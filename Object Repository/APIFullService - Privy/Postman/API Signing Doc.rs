<?xml version="1.0" encoding="UTF-8"?>
<WebServiceRequestEntity>
   <description></description>
   <name>API Signing Doc</name>
   <tag></tag>
   <elementGuidId>b2c0a59d-c80d-4f91-96e7-d6cecd718054</elementGuidId>
   <selectorMethod>BASIC</selectorMethod>
   <useRalativeImagePath>false</useRalativeImagePath>
   <connectionTimeout>-1</connectionTimeout>
   <followRedirects>false</followRedirects>
   <httpBody></httpBody>
   <httpBodyContent>{
  &quot;text&quot;: &quot;{\n    \&quot;documents\&quot;: [\n        {\n            \&quot;docToken\&quot;: \&quot;fd0709323032f19f18ae8c2ecd201212646afcb4b07914765f00ec2688a61b52\&quot;,\n            \&quot;x\&quot;: \&quot;20\&quot;,\n            \&quot;y\&quot;: \&quot;278\&quot;,\n            \&quot;page\&quot;: \&quot;4\&quot;\n        }\n    ],\n    \&quot;signature\&quot;: {\n        \&quot;visibility\&quot;: true\n    }\n}&quot;,
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
      <webElementGuid>a3caff34-f207-4ad5-bbf8-431c4b2f278d</webElementGuid>
   </httpHeaderProperties>
   <httpHeaderProperties>
      <isSelected>false</isSelected>
      <matchCondition>equals</matchCondition>
      <name>Merchant-Key</name>
      <type>Main</type>
      <value>${merchant-key}</value>
      <webElementGuid>ebe182e4-4722-4aab-8518-7a8015d034b2</webElementGuid>
   </httpHeaderProperties>
   <httpHeaderProperties>
      <isSelected>false</isSelected>
      <matchCondition>equals</matchCondition>
      <name>Token</name>
      <type>Main</type>
      <value>${access_token}</value>
      <webElementGuid>9854561a-1e4b-4711-93cd-248e92bf0ef3</webElementGuid>
   </httpHeaderProperties>
   <katalonVersion>8.5.5</katalonVersion>
   <maxResponseSize>-1</maxResponseSize>
   <migratedVersion>5.4.1</migratedVersion>
   <restRequestMethod>POST</restRequestMethod>
   <restUrl>https://stg-core.privy.id/v1/merchant/document/multiple-signing</restUrl>
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
      <id>32039e80-a196-47be-a06d-8ca3f7a4eb76</id>
      <masked>false</masked>
      <name>merchant-key</name>
   </variables>
   <variables>
      <defaultValue>GlobalVariable.access_token</defaultValue>
      <description></description>
      <id>c9749fe0-1693-47f2-b230-168f4f085770</id>
      <masked>false</masked>
      <name>access_token</name>
   </variables>
   <wsdlAddress></wsdlAddress>
</WebServiceRequestEntity>
