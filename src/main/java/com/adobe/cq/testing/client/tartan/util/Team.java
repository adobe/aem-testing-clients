/*
 * Copyright 2017 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.cq.testing.client.tartan.util;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private List<TeamMember> members;

    public Team() {
        this.members = new ArrayList<>();
    }

    public Team(TeamMember... members) {
        for (TeamMember member : members) {
            addTeamMember(member);
        }
    }

    public void addTeamMember(TeamMember member) {
        this.members.add(member);
    }

    public void addTeamMember(String userId, String roleId, String message) {
        addTeamMember(new TeamMember(userId, roleId, message));
    }

    public void removeTeamMember(TeamMember member) {
        if (members.contains(member)) {
            members.remove(member);
        }
    }

    public List<TeamMember> getMembers() {
        return this.members;
    }
}
