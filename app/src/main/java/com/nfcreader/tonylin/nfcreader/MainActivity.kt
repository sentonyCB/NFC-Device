package com.nfcreader.tonylin.nfcreader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.codec.binary.Hex
import kotlin.text.Charsets.UTF_8

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()
        //this hexString is the request, request for the transaction to begin given the info such as the amount of money there is on account
        //max 65kb
        val hexString = "00A4040007A0000002471001" + toHexString(remaining_amount.text.toString().toByteArray(UTF_8))
        val response = isoDep.transceive(hexStringToByteArray(
                hexString))
        runOnUiThread {
            //response comes back, displays "receipt"
            if (String(Hex.decodeHex(toHexString(response).toCharArray()), UTF_8).toInt() <= remaining_amount.text.toString().toInt()) {
                textView.append("\nAccepted, remaining amount: " + String(Hex.decodeHex(toHexString(response).toCharArray()), UTF_8))
                remaining_amount.text = String(Hex.decodeHex(toHexString(response).toCharArray()), UTF_8)
            } else {
                textView.append("\nRejected for insufficient funds, required amount: " + String(Hex.decodeHex(toHexString(response).toCharArray()), UTF_8))
            }
        }
        isoDep.close()
    }
}
