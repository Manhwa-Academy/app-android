<?php
/**
 * User Model
 *
 * @version 1.1 (Fixed)
 * @author Onelab
 */

class UserModel extends DataEntry
{
    public function __construct($uniqid = 0)
    {
        parent::__construct();
        if ($uniqid) {
            $this->select($uniqid);
        }
    }

    /**
     * Select user by id or email ONLY
     */
    public function select($uniqid)
    {
        if (is_int($uniqid) || ctype_digit($uniqid)) {
            $col = "id";
        } elseif (filter_var($uniqid, FILTER_VALIDATE_EMAIL)) {
            $col = "email";
        } else {
            return $this; // ❗ KHÔNG QUERY CỘT KHÔNG TỒN TẠI
        }

        $query = DB::table(TABLE_PREFIX . TABLE_USERS)
            ->where($col, "=", $uniqid)
            ->limit(1);

        if ($query->count() == 1) {
            $row = $query->get()[0];
            foreach ($row as $field => $value) {
                $this->set($field, $value);
            }
            $this->is_available = true;
        } else {
            $this->is_available = false;
        }

        return $this;
    }

    /**
     * Default values
     */
    public function extendDefaults()
    {
        $defaults = [
            "email"      => "",
            "password"   => "",
            "first_name" => "",
            "last_name"  => "",
            "role"       => "member",
            "active"     => 1,
            "create_at"  => date("Y-m-d H:i:s"),
            "update_at"  => date("Y-m-d H:i:s"),
        ];

        foreach ($defaults as $field => $value) {
            if ($this->get($field) === null) {
                $this->set($field, $value);
            }
        }
    }

    /**
     * Insert new user
     */
    public function insert()
    {
        if ($this->isAvailable()) {
            return false;
        }

        $this->extendDefaults();

        $id = DB::table(TABLE_PREFIX . TABLE_USERS)->insert([
            "email"      => $this->get("email"),
            "password"   => $this->get("password"),
            "first_name" => $this->get("first_name"),
            "last_name"  => $this->get("last_name"),
            "role"       => $this->get("role"),
            "active"     => $this->get("active"),
            "create_at"  => $this->get("create_at"),
            "update_at"  => $this->get("update_at"),
        ]);

        $this->set("id", $id);
        $this->markAsAvailable();
        return $id;
    }

    /**
     * Update user
     */
    public function update()
    {
        if (!$this->isAvailable()) {
            return false;
        }

        DB::table(TABLE_PREFIX . TABLE_USERS)
            ->where("id", "=", $this->get("id"))
            ->update([
                "email"      => $this->get("email"),
                "password"   => $this->get("password"),
                "first_name" => $this->get("first_name"),
                "last_name"  => $this->get("last_name"),
                "role"       => $this->get("role"),
                "active"     => $this->get("active"),
                "update_at"  => date("Y-m-d H:i:s"),
            ]);

        return $this;
    }

    /**
     * Delete user
     */
    public function delete()
    {
        if (!$this->isAvailable()) {
            return false;
        }

        DB::table(TABLE_PREFIX . TABLE_USERS)
            ->where("id", "=", $this->get("id"))
            ->delete();

        $this->is_available = false;
        return true;
    }

    /**
     * Check admin
     */
    public function isAdmin()
    {
        return $this->isAvailable() &&
            in_array($this->get("role"), ["admin", "developer"]);
    }

    /**
     * Permission check
     */
    public function canEdit(UserModel $User)
    {
        if (!$this->isAvailable()) {
            return false;
        }

        if ($this->get("role") === "developer") {
            return true;
        }

        if ($this->get("id") === $User->get("id")) {
            return true;
        }

        if (
            $this->get("role") === "admin" &&
            in_array($User->get("role"), ["member", "admin"])
        ) {
            return true;
        }

        return false;
    }
}
