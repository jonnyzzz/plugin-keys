package com.github.jonnyzzz.pluginkeys.services

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.LicensingFacade
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

class MyAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val allStamps = LicensingFacade.getInstance()?.confirmationStamps?.values?.distinct() ?: listOf()

        val keys = allStamps.filter { it.startsWith("key:") }
        println("There are following licenses KEYS in the current IDE: " + keys.joinToString(""){"\n    $it"})

        val stamp = allStamps.filter { it.startsWith("stamp:") }

        // 'timestampLong':'machineId':'signatureType':'signatureBase64':'certificateBase64'[:'intermediate-certificateBase64']
        // the signed part is 'timestampLong':'machineId'       machineId should be the same as PermanentInstallationID.get() returns

        val machineId = PermanentInstallationID.get()
        println("Machine ID: $machineId")

        println("There are following licenses STAMPS in the current IDE:")
        for (s in allStamps) {
            println("\n\n====================\n  $s")
            val parts = s.split(":")
            val (stamp, timestampLong, actualMachineId, signatureType, signatureBase64) = parts

            parts.drop(5)
                    .forEach { text ->
                        kotlin.runCatching {

                            val bytes = Base64.getMimeDecoder().decode(text)
                            val certificate = java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(bytes.inputStream()) as X509Certificate
                            val holder = JcaX509CertificateHolder(certificate)

                            // Get the common fields
                            println("Version: ${certificate.version}")
                            println("Serial Number: ${certificate.serialNumber}")
                            println("Issuer: ${certificate.issuerDN}")
                            println("Subject: ${certificate.subjectDN}")
                            println("Not Before: ${certificate.notBefore}")
                            println("Not After: ${certificate.notAfter}")

                            // Get the standard extensions
                            val basicConstraints = holder.extensions.getExtension(Extension.basicConstraints)?.parsedValue
                            println("Basic Constraints: $basicConstraints")

                            holder.extensions.extensionOIDs.associateWith { holder.extensions.getExtensionParsedValue(it) }
                                    .forEach { (k, v) ->
                                        println("Extension $k -> $v")
                                    }
                        }.getOrElse {
                            println("Error: $it")
                            println("Failed to process: $text. ")
                        }
                    }
        }
    }
}

/// Example output (redaxted)
/*

Machine ID: 255cc4472c348804
There are following licenses STAMPS in the current IDE:
  stamp:<drop>>
Error: java.security.cert.CertificateException: Could not parse certificate: java.io.IOException: Empty input
Failed to process: fpikDJIRsozlY+PUB...

Version: 3
Serial Number: 2998852822926357126047./..
Issuer: CN=lsrv-prod-till-20730524-intermediate
Subject: CN=55lzszcg7223.lsrv.jetbrains.com
Not Before: Thu Mar 07 14:21:14 CET 2024
Not After: Mon Mar 11 12:59:43 CET 2024
Basic Constraints: null
Extension 2.5.29.15 -> #030203A8
Extension 2.5.29.37 -> [1.3.6.1.5.5.7.3.1, 1.3.6.1.5.5.7.3.2]
Extension 2.5.29.14 -> #b2e815a6fbaeedd434c2fa993dad4137d3db15e0
Extension 2.5.29.35 -> [[CONTEXT 0]#9f8fff39ba19a0764c9d6da4f35471d92fe0b9d4]
Extension 2.5.29.17 -> [[CONTEXT 2]#35356c7a737a636737332e6c7372762e6a6574627261696e732e636f6d]

Version: 3
Serial Number: 9
Issuer: CN=License Servers CA
Subject: CN=lsrv-prod-till-20730524-intermediate
Not Before: Tue May 24 18:23:24 CEST 2022
Not After: Wed May 24 18:23:24 CEST 2073
Basic Constraints: [TRUE]
Extension 2.5.29.19 -> [TRUE]
Extension 2.5.29.14 -> #9f8fff39ba19a0764c9d6da4f35471d92fe0b9d4
Extension 2.5.29.35 -> [[CONTEXT 0]#a6c462133faebe1e93b10aaeaedc1731de09f151, [CONTEXT 1][CONTEXT 4][[[2.5.4.3, License Servers CA]]], [CONTEXT 2]#00c0ab5bd1d5fa18d9]
Extension 2.5.29.15 -> #03020106
  key:8U7XMIGT5Q-<redacted>


  The key contains the following data:

  {"licenseId":"8U7Xw434","licenseeName":"JetBrains Team","licenseeType":"COMMERCIAL","assigneeName":"Eugene Petrenko","assigneeEmail":"eugene.petrenko@jetbrains.com","licenseRestriction":"","checkConcurrentUse":true,"products":[{"code":"GO","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"RS0","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"DM","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"CL","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"AC","fallbackDate":"2023-12-31","paidUpTo":"2026-12-28","extended":false},{"code":"RSU","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"RSC","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true},{"code":"PC","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"DS","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"RD","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"RC","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"RSF","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true},{"code":"RM","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"II","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"DPN","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"DB","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"DC","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"PS","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"RSV","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true},{"code":"WS","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":false},{"code":"PSI","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true},{"code":"PCWMP","paidUpTo":"2026-12-28","extended":true},{"code":"AIL","paidUpTo":"2026-12-28","extended":false},{"code":"RS","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true},{"code":"DP","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true},{"code":"PDB","fallbackDate":"2025-12-29","paidUpTo":"2026-12-28","extended":true}],"metadata":"0120231122CPJA012010","hash":"35370650/68738:1619231062","gracePeriodDays":7,"autoProlongated":false,"isAutoProlongated":false,"trial":false,"aiAllowed":true}


*/