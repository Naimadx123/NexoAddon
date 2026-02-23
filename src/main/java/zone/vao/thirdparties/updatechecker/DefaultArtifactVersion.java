package zone.vao.thirdparties.updatechecker;

import org.jetbrains.annotations.NotNull;

class DefaultArtifactVersion implements ArtifactVersion {
    private ComparableVersion comparable;

    public DefaultArtifactVersion(final String version) {
        this.parseVersion(version);
    }

    @Override
    public int hashCode() {
        return 11 + this.comparable.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || (other instanceof ArtifactVersion && this.compareTo((ArtifactVersion)other) == 0);
    }

    @Override
    public int compareTo(final @NotNull ArtifactVersion otherVersion) {
        if (otherVersion instanceof DefaultArtifactVersion) {
            return this.comparable.compareTo(((DefaultArtifactVersion)otherVersion).comparable);
        }
        return this.compareTo(new DefaultArtifactVersion(otherVersion.toString()));
    }

    public final void parseVersion(final String version) {
        this.comparable = new ComparableVersion(version);
    }

    @Override
    public String toString() {
        return this.comparable.toString();
    }
}