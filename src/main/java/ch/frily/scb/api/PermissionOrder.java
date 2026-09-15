package ch.frily.scb.api;

import net.dv8tion.jda.api.Permission;

/**
 * Defines the permission order of {@link Permission}
 * <ul>
 *     <li>The {@link Permission#isGuild()} propertie defines whether the permission is present on a guild (channel, role, ...)</li>
 *     <li>The {@link Permission#isChannel()} propertie defines whether the permission is present in channels (if true), or present for roles (if false)</li>
 * </ul>
 */
public enum PermissionOrder {
    VIEW_CHANNEL(Permission.VIEW_CHANNEL, 0);

    final Permission permission;

    // The position can change depending on the calling permissionHolder (TextChannel, VoiceChannel, Category, ...)
    final int position;

    PermissionOrder(final Permission permission, final int position) {
        this.permission = permission;
        this.position = position;
    }
}
