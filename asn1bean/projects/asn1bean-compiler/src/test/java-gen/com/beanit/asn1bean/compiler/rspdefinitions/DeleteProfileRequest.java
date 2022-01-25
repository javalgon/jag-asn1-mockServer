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

public class DeleteProfileRequest implements BerType, Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] code = null;
	public static final BerTag tag = new BerTag(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 51);

	public OctetTo16 isdpAid = null;
	public Iccid iccid = null;
	
	public DeleteProfileRequest() {
	}

	public DeleteProfileRequest(byte[] code) {
		this.code = code;
	}

	public DeleteProfileRequest(OctetTo16 isdpAid, Iccid iccid) {
		this.isdpAid = isdpAid;
		this.iccid = iccid;
	}

	@Override public int encode(OutputStream reverseOS) throws IOException {
		return encode(reverseOS, true);
	}

	public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

		if (code != null) {
			reverseOS.write(code);
			if (withTag) {
				return tag.encode(reverseOS) + code.length;
			}
			return code.length;
		}

		int codeLength = 0;
		if (iccid != null) {
			codeLength += iccid.encode(reverseOS, true);
			codeLength += BerLength.encodeLength(reverseOS, codeLength);
			if (withTag) {
				codeLength += tag.encode(reverseOS);
			}
			return codeLength;
		}
		
		if (isdpAid != null) {
			codeLength += isdpAid.encode(reverseOS, false);
			// write tag: APPLICATION_CLASS, PRIMITIVE, 15
			reverseOS.write(0x4F);
			codeLength += 1;
			codeLength += BerLength.encodeLength(reverseOS, codeLength);
			if (withTag) {
				codeLength += tag.encode(reverseOS);
			}
			return codeLength;
		}
		
		throw new IOException("Error encoding CHOICE: No element of CHOICE was selected.");
	}

	@Override public int decode(InputStream is) throws IOException {
		return decode(is, true);
	}

	public int decode(InputStream is, boolean withTag) throws IOException {
		int tlvByteCount = 0;
		BerTag berTag = new BerTag();

		if (withTag) {
			tlvByteCount += tag.decodeAndCheck(is);
		}

		BerLength explicitTagLength = new BerLength();
		tlvByteCount += explicitTagLength.decode(is);
		tlvByteCount += berTag.decode(is);

		if (berTag.equals(BerTag.APPLICATION_CLASS, BerTag.PRIMITIVE, 15)) {
			isdpAid = new OctetTo16();
			tlvByteCount += isdpAid.decode(is, false);
			tlvByteCount += explicitTagLength.readEocIfIndefinite(is);
			return tlvByteCount;
		}

		if (berTag.equals(Iccid.tag)) {
			iccid = new Iccid();
			tlvByteCount += iccid.decode(is, false);
			tlvByteCount += explicitTagLength.readEocIfIndefinite(is);
			return tlvByteCount;
		}

		throw new IOException("Error decoding CHOICE: Tag " + berTag + " matched to no item.");
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);
		encode(reverseOS, false);
		code = reverseOS.getArray();
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		appendAsString(sb, 0);
		return sb.toString();
	}

	public void appendAsString(StringBuilder sb, int indentLevel) {

		if (isdpAid != null) {
			sb.append("isdpAid: ").append(isdpAid);
			return;
		}

		if (iccid != null) {
			sb.append("iccid: ").append(iccid);
			return;
		}

		sb.append("<none>");
	}

}
