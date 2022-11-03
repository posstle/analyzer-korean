package org.fastcatsearch.plugin.analysis.ko;

import org.fastcatsearch.ir.dic.Dictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.LicenseInvalidException;
import org.fastcatsearch.plugin.PluginLicenseInfo;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.plugin.analysis.AnalyzerInfo;
import org.fastcatsearch.plugin.analysis.ko.standard.StandardKoreanAnalyzerFactory;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagProbDictionary;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class KoreanAnalysisPlugin extends AnalysisPlugin<TagProb, PreResult<CharVector>> {

    public KoreanAnalysisPlugin(File pluginDir, PluginSetting pluginSetting, String serverId) {
        super(pluginDir, pluginSetting, serverId);


    }

    @Override
    protected void doLoad(boolean isMasterNode) throws LicenseInvalidException {
        //License 검증.
        validateLicense();

        super.doLoad(isMasterNode);
    }

    private void validateLicense() throws LicenseInvalidException {
        if (licenseKey == null) {
            throw new LicenseInvalidException("License key is not found.");
        }
        String licenseData = getDecryptString(licenseKey);
        String[] data = licenseData.split("\n");
        if (data.length != 4) {
            throw new LicenseInvalidException("License key format is corrupt.");
        }

        String licenseProductName = data[0];
        String licenseServerId = data[1];
        String licenseExpireDate = data[2];
        String licensee = data[3];

        if (!licenseProductName.equals("analyzer-korean")) {
            throw new LicenseInvalidException("This license is not for analyzer-korean.");
        }
        if (licenseServerId.equals("Everywhere")) {
            //ok
        } else if (!licenseServerId.equals(serverId)) {
            throw new LicenseInvalidException("This license is not for server id '" + serverId + "'. Please renew the license.");
        }
        /*
        licenseTime format
        1. Unlimited
        2. yyyy-MM-dd
        */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (licenseExpireDate.equals("Unlimited")) {
            //OK!!
        } else {
            Date expireDate = null;
            try {
                expireDate = sdf.parse(licenseExpireDate);
            } catch (ParseException e) {
                throw new LicenseInvalidException("License key format is corrupt.");
            }

            //오늘보다 이전이면 파기됨.
            if (expireDate.before(new Date())) {
                throw new LicenseInvalidException("This license is expired at " + expireDate);
            }
        }

        setLicenseInfo(new PluginLicenseInfo(licenseProductName, licenseExpireDate, licensee));
    }

    @Override
    protected void loadAnalyzerFactory(Map<String, AnalyzerInfo> analyzerFactoryMap) {
        registerAnalyzer(analyzerFactoryMap, "standard", "Standard Korean Analyzer", new StandardKoreanAnalyzerFactory(commonDictionary));
    }

    @Override
    protected Dictionary<TagProb, PreResult<CharVector>> loadSystemDictionary(DictionarySetting dictionarySetting) {
        File systemDictFile = getDictionaryFile(dictionarySetting.getId());
        long st = System.nanoTime();
        TagProbDictionary tagProbDictionary = new TagProbDictionary(systemDictFile, dictionarySetting.isIgnoreCase());
        logger.debug("Korean Dictionary Load {}ms >> {}", (System.nanoTime() - st) / 1000000, systemDictFile.getName());

        return tagProbDictionary;
    }

    private final String getDecryptString(String encryptedString) {
        try {
            SecretKeySpec skeySpec = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(decodeHexString(encryptedString));
            return new String(original);
        } catch (Exception e) {
            //ignore
        }
        return null;
    }

    private final SecretKeySpec getSecretKeySpec() {
        String symKeyHex = "630B3720F5A705374B9FA7BFBCAF4FF9";
        final byte[] symKeyData = DatatypeConverter.parseHexBinary(symKeyHex);
        return new SecretKeySpec(new SecretKeySpec(symKeyData, "AES").getEncoded(), "AES");
    }

    private static byte[] decodeHexString(final String string) {
        final int l = string.length();
        byte[] data = new byte[l >> 1];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ((Character.digit(string.charAt(i * 2), 16) << 4) + Character.digit(string.charAt(i * 2 + 1), 16));
        }

        return data;
    }
}
