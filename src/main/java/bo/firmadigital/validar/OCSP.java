/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.validar;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.cert.ocsp.UnknownStatus;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

/**
 *
 * @author ADSIB
 */
public class OCSP {
    /**
     * Crea una solicitud de estado OCSP para el serial del certificado proporcionado
     * @param issuer Certificado de la autoridad que emitió el certificado
     * @param serial Número de serie del certificado a consultar
     * @return Solicitud de estado OCSP
     * @throws Exception 
     */
    public static OCSPReq build(X509Certificate issuer, BigInteger serial) throws Exception {
        try {
            OCSPReqBuilder builder = new OCSPReqBuilder();
            JcaDigestCalculatorProviderBuilder digestCalculatorProviderBuilder = new JcaDigestCalculatorProviderBuilder();
            DigestCalculator calculator = digestCalculatorProviderBuilder.build().get(CertificateID.HASH_SHA1);
            CertificateID certId = new CertificateID(calculator, new X509CertificateHolder(issuer.getEncoded()), serial);
            builder.addRequest(certId);
            return builder.build();
        } catch (IOException | OperatorCreationException | CertificateEncodingException | OCSPException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Envía una consulta OCSP
     * @param serviceUrl Url del servicio OCSP
     * @param request Solicitud a enviar
     * @return Respuesta del servicio OCSP
     * @throws Exception 
     */
    public static OCSPResp send(String serviceUrl, OCSPReq request) throws Exception {
        try {
            byte[] ocspReqData = request.getEncoded();
            URLConnection conn = new URL(serviceUrl).openConnection();
            conn.setRequestProperty("Content-Type", "application/ocsp-request");
            conn.setRequestProperty("Accept", "application/ocsp-response");
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            try (DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out))) {
                dataOut.write(ocspReqData);
                dataOut.flush();
            }
            InputStream in = (InputStream) conn.getInputStream();
            OCSPResp ocspResp = new OCSPResp(in);
            return ocspResp;
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Obtiene el valor de una extención en un certificado
     * @param certificate El certificado del que se obtendrá la extensión
     * @param oid the Identificador de la extensión que se desea obtener.
     * @return El valor de la extensión como objeto ASN1Primitive
     * @throws IOException
     */
    private static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
        byte[] bytes = certificate.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    /**
     * Obtiene la url OCSP de un certificado
     * @param certificate Certificado del que se obtendrá la url OCSP
     * @return Retorna la url OCSP o null en caso de no encontrarse
     * @throws IOException
     */
    public static String getOCSPUrl(X509Certificate certificate) throws IOException {
        ASN1Primitive obj;
        try {
            obj = getExtensionValue(certificate, Extension.authorityInfoAccess.getId());
        } catch (IOException ex) {
            return null;
        }

        if (obj == null) {
            return null;
        }

        AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(obj);

        AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
        for (AccessDescription accessDescription : accessDescriptions) {
            boolean correctAccessMethod = accessDescription.getAccessMethod().equals(X509ObjectIdentifiers.ocspAccessMethod);
            if (!correctAccessMethod) {
                continue;
            }

            GeneralName name = accessDescription.getAccessLocation();
            if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
                continue;
            }

            DERIA5String derStr = DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
            return derStr.getString();
        }

        return null;
    }

    /**
     * Verifica el estado del certificado
     * @param cert Certificado del que se desea conocer su estado
     * @param issuer Certificado de la autoridad que emitió el certificado
     * @return Cadena descriptiva del estado de revocación
     */
    public static String check(X509Certificate cert, X509Certificate issuer) {
        try {
            OCSPReq request = build(issuer, cert.getSerialNumber());
            OCSPResp ocspResp = send(getOCSPUrl(cert), request);
            if (ocspResp.getStatus() == OCSPRespBuilder.SUCCESSFUL) {
                BasicOCSPResp basic = (BasicOCSPResp) ocspResp.getResponseObject();
                SingleResp[] resps = basic.getResponses();
                if (resps != null && resps.length == 1) {
                    SingleResp resp = resps[0];
                    CertificateStatus certStatus = resp.getCertStatus();
                    if (certStatus == CertificateStatus.GOOD) {
                        return "Certificado no revocado";
                    } else {
                        if (certStatus instanceof RevokedStatus) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            return "Certificado revocado  el " + dateFormat.format(((RevokedStatus) certStatus).getRevocationTime());
                        } else if (certStatus instanceof UnknownStatus) {
                            return "Estado desconocido";
                        }
                    }
                } else {
                    return "No se pudo verificar el estado";
                }
            } else {
                return "No se pudo verificar el estado";
            }
        } catch (Exception ex) {
            return "No se pudo verificar el estado";
        }
        return null;
    }
}
