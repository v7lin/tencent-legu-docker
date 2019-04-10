package io.github.v7lin;

import com.tencentcloudapi.ms.v20180408.models.CreateCosSecKeyInstanceResponse;
import io.github.v7lin.task.CreateCosSecKeyTask;
import io.github.v7lin.task.CreateShieldTask;
import io.github.v7lin.task.UploadTask;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.IconFace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.List;

@RunWith(JUnit4.class)
public final class LeGuTest {

    @Test
    public void test() throws Exception {
        String secretId = System.getenv("TENCENT_SECRET_ID");
        String secretKey = System.getenv("TENCENT_SECRET_KEY");
        String region = System.getenv("TENCENT_REGION");

        File projectDir = new File("apk");

        File[] apks = projectDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".apk");
            }
        });

        if (apks == null || apks.length == 0) {
            throw new RuntimeException("can't find apk");
        }

        File apk = apks[0];

        ApkFile apkFile = new ApkFile(apk);

        ApkMeta apkMeta = apkFile.getApkMeta();

        // icon
        List<IconFace> iconFaces = apkFile.getAllIcons();
        if (iconFaces == null || iconFaces.isEmpty()) {
            throw new RuntimeException(apkMeta.getName() + "(" + apk.getName() + ")" + " doesn't have icon");
        }

        // 上传文件到COS文件存储
        CreateCosSecKeyInstanceResponse resp = new CreateCosSecKeyTask(secretId, secretKey, region).execute();
        URL apkUrl = new UploadTask(resp, apk, apkMeta).execute();
        System.out.println("apk url: " + apkUrl.toString());

        // 加固
        URL leguApkUrl = new CreateShieldTask(secretId, secretKey, region, apk, apkMeta, apkUrl).execute();
        System.out.println("legu apk url: " + leguApkUrl.toString());
    }
}