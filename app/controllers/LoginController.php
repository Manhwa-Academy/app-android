<?php
/**
 * Login Controller
 */
class LoginController extends Controller
{
    /**
     * Process
     */
    public function process()
    {
        $AuthUser = $this->getVariable("AuthUser");
        if ($AuthUser) {
            $this->resp->result = 1;
            $this->resp->msg = __("You already logged in");
            $this->jsonecho();
        }


        $this->login();
    }


    /**
     * Login
     * @return void
     */
    private function login()
    {
        $this->resp->result = 0;

        $email = Input::post("email");
        $password = Input::post("password");

        if (!$email || !$password) {
            $this->resp->msg = __("Email and password are required !");
            $this->jsonecho();
        }

        // Find user by email
        $User = Controller::model("User", $email);

        // ❌ Email không tồn tại
        if (!$User->isAvailable()) {
            $this->resp->msg = __("Email does not exist !");
            $this->jsonecho();
        }

        // ❌ Sai mật khẩu (bcrypt)
        if (!password_verify($password, $User->get("password"))) {
            $this->resp->msg = __("Password is incorrect !");
            $this->jsonecho();
        }

        // ❌ Tài khoản chưa active
        if ((int) $User->get("active") !== 1) {
            $this->resp->msg = __("Account is not activated !");
            $this->jsonecho();
        }

        // ✅ LOGIN THÀNH CÔNG
        $data = [
            "id" => (int) $User->get("id"),
            "email" => $User->get("email"),
            "first_name" => $User->get("first_name"),
            "last_name" => $User->get("last_name"),
            "role" => $User->get("role"),
            "active" => (int) $User->get("active"),
            "create_at" => $User->get("create_at"),
            "update_at" => $User->get("update_at")
        ];

        $payload = $data;
        $payload["iat"] = time(); // issued at

        // ⚠️ KHÔNG nhét password vào JWT
        $jwt = Firebase\JWT\JWT::encode($payload, EC_SALT, 'HS256');

        $this->resp->result = 1;
        $this->resp->msg = __("Log in successfully");
        $this->resp->accessToken = $jwt;
        $this->resp->data = $data;

        $this->jsonecho();
    }
}