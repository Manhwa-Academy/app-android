<?php
class ProfileController extends Controller
{
    public function process()
    {
        $AuthUser = $this->getVariable("AuthUser");

        // ✅ AUTH CHECK
        if (!$AuthUser) {
            $this->resp->result = 0;
            $this->resp->msg = "Unauthorized";
            $this->jsonecho();
            return;
        }

        $method = Input::method();

        if ($method === 'GET') {
            $this->getProfile();
        } else if ($method === 'POST') {
            $this->changeInformation();
        }
    }

    /* ===================== GET PROFILE ===================== */
    private function getProfile()
    {
        $AuthUser = $this->getVariable("AuthUser");

        $user = DB::table(TABLE_PREFIX . TABLE_USERS)
            ->where("id", "=", $AuthUser->get("id"))
            ->limit(1)
            ->get();

        if (count($user) === 0) {
            $this->resp->result = 0;
            $this->resp->msg = "User not found";
            $this->jsonecho();
            return;
        }

        $u = $user[0];

        $this->resp->result = 1;
        $this->resp->data = [
            "id" => (int) $u->id,
            "email" => $u->email,
            "first_name" => $u->first_name,
            "last_name" => $u->last_name,
            "phone" => $u->phone,
            "address" => $u->address,
            "role" => $u->role,
            "active" => (int) $u->active,
            "create_at" => $u->create_at,
            "update_at" => $u->update_at
        ];

        $this->jsonecho();
    }

    /* ===================== UPDATE PROFILE ===================== */
    private function changeInformation()
    {
        $AuthUser = $this->getVariable("AuthUser");

        $required = ["first_name", "last_name", "phone", "address"];
        foreach ($required as $field) {
            if (!Input::post($field)) {
                $this->resp->result = 0;
                $this->resp->msg = "Missing field: $field";
                $this->jsonecho();
                return;
            }
        }

        // ✅ LOAD USER BY ID (NOT EMAIL)
        $User = Controller::model("User", $AuthUser->get("id"));
        if (!$User->isAvailable()) {
            $this->resp->result = 0;
            $this->resp->msg = "User not found";
            $this->jsonecho();
            return;
        }

        try {
            $User->set("first_name", Input::post("first_name"))
                ->set("last_name", Input::post("last_name"))
                ->set("phone", Input::post("phone"))
                ->set("address", Input::post("address"))
                ->set("update_at", date("Y-m-d H:i:s"))
                ->save();

            $this->resp->result = 1;
            $this->resp->msg = "Change information successfully";
            $this->resp->data = [
                "id" => (int) $User->get("id"),
                "email" => $User->get("email"),
                "first_name" => $User->get("first_name"),
                "last_name" => $User->get("last_name"),
                "phone" => $User->get("phone"),
                "address" => $User->get("address"),
                "role" => $User->get("role"),
                "active" => (int) $User->get("active"),
                "create_at" => $User->get("create_at"),
                "update_at" => $User->get("update_at")
            ];

        } catch (\Exception $e) {
            $this->resp->result = 0;
            $this->resp->msg = $e->getMessage();
        }

        $this->jsonecho();
    }
}
