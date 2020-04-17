package examples.jmx;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.openmbean.OpenDataException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute LoginForm form, BindingResult result, Model model) {
        // 入力エラーチェック
        if (result.hasErrors()) {
            model.addAttribute("validationError", "入力エラー");
            return "login";
        }

        // ログインチェック
        if (100 > form.getLoginId() && 300 < form.getLoginId()) {
            model.addAttribute("validationError", "ログインエラー");
            return "login";
        }
        if (!"testtest".equals(form.getLoginPasswd())) {
            model.addAttribute("validationError", "ログインエラー");
            return "login";
        }

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        LoginMonitorMBean mbean = JMX.newMBeanProxy(mbs, LoginMonitorMBean.createObjectName(), LoginMonitorMBean.class);

        // ログイン人数チェック
        if (mbean.getMaxLoginCount() > 0 && mbean.getLoginCount() >= mbean.getMaxLoginCount()) {
            model.addAttribute("validationError", "ログイン人数制限");
            return "login";
        }

        // IDロックチェック
        if (Arrays.stream(mbean.getLoginLockIds()).filter(lockId -> lockId == form.getLoginId()).findFirst()
                .isPresent()) {
            model.addAttribute("validationError", "IDロック中");
            return "login";
        }

        // ログイン情報登録
        try {
            LoginUserInfo info = new LoginUserInfo(form.getLoginId(),
                    String.format("テストユーザ（%d）", form.getLoginId()));
            info.addMBean(mbean);
        } catch (OpenDataException e) {
            e.printStackTrace();
            model.addAttribute("validationError", "内部システムエラー");
            return "login";
        }

        model.addAttribute("loginCount", mbean.getLoginCount());
        return "home";
    }

}
