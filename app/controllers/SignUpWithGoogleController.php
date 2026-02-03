<?php
class SignUpWithGoogleController extends Controller
{
    public function process()
    {
        if (Input::method() === 'POST') {
            $this->loginWithGoogle();
        }
    }

    private function loginWithGoogle()
    {
        $this->resp->result = 0;

        /* ========= REQUIRED FIELDS ========= */
        $required_fields = ["email"];

        foreach ($required_fields as $field) {
            if (!Input::post($field)) {
                $this->resp->msg = __("Missing field: " . $field);
                $this->jsonecho();
                return;
            }
        }

        /* ========= INPUT ========= */
        $email = strtolower(trim(Input::post("email")));
        $firstName = Input::post("first_name") ?: $email;
        $lastName = Input::post("last_name") ?: "";

        /* ========= LOAD USER ========= */
        $User = Controller::model("User", $email);

        // ✅ Nếu user đã tồn tại → login luôn
        if ($User->isAvailable()) {

            if ((int) $User->get("active") !== 1) {
                $this->resp->msg = __("Account is not activated !");
                $this->jsonecho();
                return;
            }

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

        } else {
            // ✅ User chưa tồn tại → tạo mới (Google signup)

            $User->set("email", $email)
                ->set("password", password_hash(bin2hex(random_bytes(8)), PASSWORD_DEFAULT))
                ->set("first_name", $firstName)
                ->set("last_name", $lastName)
                ->set("role", "member")
                ->set("active", 1)
                ->set("create_at", date("Y-m-d H:i:s"))
                ->set("update_at", date("Y-m-d H:i:s"))
                ->save();

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
        }

        /* ========= JWT ========= */
        $payload = $data;
        $payload["iat"] = time();

        $jwt = Firebase\JWT\JWT::encode($payload, EC_SALT, 'HS256');

        $this->resp->result = 1;
        $this->resp->msg = __("Log in with Google successfully");
        $this->resp->accessToken = $jwt;
        $this->resp->data = $data;

        $this->jsonecho();
    }
}
