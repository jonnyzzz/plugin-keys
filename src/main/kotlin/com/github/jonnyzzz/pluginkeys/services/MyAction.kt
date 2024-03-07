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