/*
 * This class file was automatically generated by ASN1bean (http://www.beanit.com)
 */

package com.beanit.asn1bean.compiler.rspdefinitions;

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.io.Serializable;
import com.beanit.asn1bean.ber.*;
import com.beanit.asn1bean.ber.types.*;
import com.beanit.asn1bean.ber.types.string.*;

import com.beanit.asn1bean.compiler.pkix1explicit88.Certificate;
import com.beanit.asn1bean.compiler.pkix1explicit88.CertificateList;
import com.beanit.asn1bean.compiler.pkix1explicit88.Time;
import com.beanit.asn1bean.compiler.pkix1implicit88.SubjectKeyIdentifier;

public class PendingNotification implements BerType, Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] code = null;
	public ProfileInstallationResult profileInstallationResult = null;
	public OtherSignedNotification otherSignedNotification = null;
	
	public PendingNotification() {
	}

	public PendingNotification(byte[] code) {
		this.code = code;
	}

	public PendingNotification(ProfileInstallationResult profileInstallationResult, OtherSignedNotification otherSignedNotification) {
		this.profileInstallationResult = profileInstallationResult;
		this.otherSignedNotification = otherSignedNotification;
	}

	@Override public int encode(OutputStream reverseOS) throws IOException {

		if (code != null) {
			reverseOS.write(code);
			return code.length;
		}

		int codeLength = 0;
		if (otherSignedNotification != null) {
			codeLength += otherSignedNotification.encode(reverseOS, true);
			return codeLength;
		}
		
		if (profileInstallationResult != null) {
			codeLength += profileInstallationResult.encode(reverseOS, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 55
			reverseOS.write(0x37);
			reverseOS.write(0xBF);
			codeLength += 2;
			return codeLength;
		}
		
		throw new IOException("Error encoding CHOICE: No element of CHOICE was selected.");
	}

	@Override public int decode(InputStream is) throws IOException {
		return decode(is, null);
	}

	public int decode(InputStream is, BerTag berTag) throws IOException {

		int tlvByteCount = 0;
		boolean tagWasPassed = (berTag != null);

		if (berTag == null) {
			berTag = new BerTag();
			tlvByteCount += berTag.decode(is);
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 55)) {
			profileInstallationResult = new ProfileInstallationResult();
			tlvByteCount += profileInstallationResult.decode(is, false);
			return tlvByteCount;
		}

		if (berTag.equals(OtherSignedNotification.tag)) {
			otherSignedNotification = new OtherSignedNotification();
			tlvByteCount += otherSignedNotification.decode(is, false);
			return tlvByteCount;
		}

		if (tagWasPassed) {
			return 0;
		}

		throw new IOException("Error decoding CHOICE: Tag " + berTag + " matched to no item.");
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);
		encode(reverseOS);
		code = reverseOS.getArray();
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		appendAsString(sb, 0);
		return sb.toString();
	}

	public void appendAsString(StringBuilder sb, int indentLevel) {

		if (profileInstallationResult != null) {
			sb.append("profileInstallationResult: ");
			profileInstallationResult.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (otherSignedNotification != null) {
			sb.append("otherSignedNotification: ");
			otherSignedNotification.appendAsString(sb, indentLevel + 1);
			return;
		}

		sb.append("<none>");
	}

}
