<?php
/**
 * Signup Controller
 */

class SignupController extends Controller
{
    public function process()
    {
        $AuthUser = $this->getVariable("AuthUser");
        if ($AuthUser) {
            $this->resp->result = 1;
            $this->resp->msg = __("You have been logging in !");
            $this->jsonecho();
            return;
        }

        $this->signup();
    }

    private function signup()
    {
        $this->resp->result = 0;

        /* ========= 1. CHECK REQUIRED FIELDS ========= */
        $required_fields = ["email", "password", "password-confirm"];
        foreach ($required_fields as $field) {
            if (!Input::post($field)) {
                $this->resp->msg = __("Missing field: " . $field);
                $this->jsonecho();
                return;
            }
        }

        /* ========= 2. GET INPUT ========= */
        $email = strtolower(trim(Input::post("email")));
        $password = Input::post("password");
        $firstName = Input::post("first_name") ?: $email;
        $lastName = Input::post("last_name") ?: "";

        /* ========= 3. VALIDATE EMAIL ========= */
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $this->resp->msg = __("Email is not valid!");
            $this->jsonecho();
            return;
        }

        $User = Controller::model("User", $email);
        if ($User->isAvailable()) {
            $this->resp->msg = __("This email is used by someone!");
            $this->jsonecho();
            return;
        }

        /* ========= 4. VALIDATE PASSWORD ========= */
        if (mb_strlen($password) < 6) {
            $this->resp->msg = __("Password must be at least 6 characters!");
            $this->jsonecho();
            return;
        }

        if (Input::post("password-confirm") !== $password) {
            $this->resp->msg = __("Password confirmation didn't match!");
            $this->jsonecho();
            return;
        }

        /* ========= 5. SAVE USER ========= */
        try {
            $User->set("email", $email)
                ->set("password", password_hash($password, PASSWORD_DEFAULT))
                ->set("first_name", $firstName)
                ->set("last_name", $lastName)
                ->set("role", "member")
                ->set("active", 1) // 🔥 QUAN TRỌNG
                ->set("create_at", date("Y-m-d H:i:s"))
                ->set("update_at", date("Y-m-d H:i:s"))
                ->save();

            /* ========= 6. RESPONSE ========= */
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


            /* ========= 7. JWT ========= */
            $payload = $data;
            $payload["iat"] = time();

            $jwt = Firebase\JWT\JWT::encode($payload, EC_SALT, 'HS256');

            $this->resp->result = 1;
            $this->resp->msg = __("Your account has been created successfully!");
            $this->resp->accessToken = $jwt;
            $this->resp->data = $data;

        } catch (\Exception $ex) {
            $this->resp->msg = $ex->getMessage();
        }

        $this->jsonecho();
    }
}
