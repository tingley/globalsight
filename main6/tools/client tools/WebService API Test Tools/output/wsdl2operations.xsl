<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
<xsl:output indent="yes"/>
<xsl:key name="messageSearch" match="wsdl:message" use="@name"/>
<xsl:strip-space elements="*"/>

	<xsl:template match="/">
		<operations>
			<xsl:apply-templates />
		</operations>
	</xsl:template>

	<xsl:template match="wsdl:portType">
	<xsl:for-each select="wsdl:operation">
	<xsl:text>
	</xsl:text>
		<operation>
			<xsl:attribute name="name">
				<xsl:value-of select="@name"></xsl:value-of>
			</xsl:attribute>
			<xsl:text>
		</xsl:text>
		
			<return>
				<xsl:attribute name="type">
					<xsl:value-of select="substring-after(string(key('messageSearch',wsdl:output/@name)/wsdl:part/@type),':')"></xsl:value-of>
				</xsl:attribute>
			</return>
			
			<xsl:for-each select="key('messageSearch',wsdl:input/@name)/wsdl:part">
			<xsl:text>
		</xsl:text>
				<param>
					<xsl:attribute name="type">
					<xsl:value-of select="substring-after(string(@type),':')"/>
					</xsl:attribute>
					<xsl:value-of select="@name"/>
				</param>
			</xsl:for-each>
			<xsl:text>
	</xsl:text>
	</operation>
	<xsl:text>
	</xsl:text>
	</xsl:for-each>	
	</xsl:template>
</xsl:stylesheet>