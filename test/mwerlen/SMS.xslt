<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <Messages>
            <xsl:for-each select="Root/Item">
                <Message>
                    <From><xsl:value-of select="eMsgHead/szFromNumber" /></From>
                    <To><xsl:value-of select="eMsgHead/szToNumber" /></To>
                    <Folder><xsl:choose>
                        <xsl:when test="eMsgHead/uBoxID=0">INBOX</xsl:when>
                        <xsl:otherwise>SENT</xsl:otherwise>
                    </xsl:choose></Folder>
                    <Date><xsl:value-of select="eMsgHead/uYear" />-<xsl:value-of select="eMsgHead/uMonth" />-<xsl:value-of select="eMsgHead/uDay" /><xsl:text> </xsl:text><xsl:value-of select="eMsgHead/uHour" />:<xsl:value-of select="eMsgHead/uMinute" />:<xsl:value-of select="eMsgHead/uSecond" /></Date>
                    <Body><xsl:value-of select="pMsgData" /></Body>
                </Message>
            </xsl:for-each>
        </Messages>
    </xsl:template>
</xsl:stylesheet>
